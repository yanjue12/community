package com.fzg.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;

import com.aliyun.auth.credentials.provider.EnvironmentVariableCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.config.AliyunSmsProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fzg.config.TencentCaptchaProperties;
import com.fzg.constant.RedisVerificationKey;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.Rolemapper;
import com.fzg.mapper.UserRolemapper;
import com.fzg.mapper.UserProfileMapper;
import com.fzg.mapper.ArticleTagMapper;
import com.fzg.model.*;
import com.fzg.service.AuditRecordService;
import com.fzg.service.INotificationService;
import com.fzg.service.SensitiveService;
import com.fzg.service.UserPrivacyService;
import com.fzg.service.UserService;
import com.fzg.mapper.UserMapper;
import com.fzg.util.UserUtil;
import com.fzg.vo.*;

import darabonba.core.client.ClientOverrideConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author yanju
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:29
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private JavaMailSender javaMailSender;
    @Resource
    private TencentCaptchaProperties tencentCaptchaProperties;
    @Resource
    private AliyunSmsProperties aliyunSmsProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private Articlemapper articlemapper;
    @Autowired
    private UserRolemapper userRolemapper;
    @Autowired
    private Rolemapper rolemapper;
    @Autowired
    private UserPrivacyService userPrivacyService;
    @Autowired
    private AuditRecordService auditRecordService;
    @Autowired
    private INotificationService notificationService;
    @Autowired
    private UserProfileMapper userProfileMapper;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Autowired
    private SensitiveService sensitiveService;


    /**
     * 用户修改隐私，可以单独文章改，也可以个人设置所有作品私密等。
     * @param upPriSetVO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePrivateSetting(UpdatePrivateSettingVO upPriSetVO) {
        log.info("UserServiceImpl.updatePrivateSetting开始修改隐私设置");

        String flag = StringUtils.defaultIfBlank(upPriSetVO.getFlag(), "ALL");
        Boolean b = false;
        //懒得搞字典，直接弄 ALL为全局设置
        if("ALL".equalsIgnoreCase(flag)){
            b = updateUserPrivacyByUserId(upPriSetVO);
        }else if("MONOMER".equalsIgnoreCase(flag)){
            //修改单篇文章设置
            if(null == upPriSetVO.getArticleId()){
                log.error("入参文章id为空");
                return false;
            }
            LambdaQueryWrapper<Article> a = new LambdaQueryWrapper<>();
            a.eq(Article::getId,upPriSetVO.getArticleId())
                    .eq(Article::getUserId,upPriSetVO.getUserId());
            Article article = articlemapper.selectOne(a);
            if(null == article){
                log.error("文章查询出为空");
                return false;
            }
            article.setVisibility(upPriSetVO.getArticleVisibility());
            article.setIsCommentable(upPriSetVO.getCanComment());
            article.setIsRecommend(upPriSetVO.getAllowRecommendation());
            b = articlemapper.updateById(article) > 0;
        }

        return b;
    }

    private Boolean updateUserPrivacyByUserId(UpdatePrivateSettingVO upPriSetVO) {
        LambdaQueryWrapper<UserPrivacy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPrivacy::getUserId, upPriSetVO.getUserId())
                .last("limit 1");
        UserPrivacy userPrivacy = userPrivacyService.getOne(wrapper);
        Date now = Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
        boolean created = false;
        if (userPrivacy == null) {
            userPrivacy = new UserPrivacy();
            userPrivacy.setUserId(upPriSetVO.getUserId());
            userPrivacy.setCreatedAt(now);
            fillDefaultPrivacySetting(userPrivacy);
            created = true;
        }

        copyNotNullPrivacySetting(upPriSetVO, userPrivacy);
        userPrivacy.setUpdatedAt(now);
        return created ? userPrivacyService.save(userPrivacy) : userPrivacyService.updateById(userPrivacy);
    }

    private void copyNotNullPrivacySetting(UpdatePrivateSettingVO source, UserPrivacy target) {
        if (source.getEmailVisibility() != null) target.setEmailVisibility(source.getEmailVisibility());
        if (source.getPhoneVisibility() != null) target.setPhoneVisibility(source.getPhoneVisibility());
        if (source.getProfileVisibility() != null) target.setProfileVisibility(source.getProfileVisibility());
        if (source.getCanComment() != null) target.setCanComment(source.getCanComment());
        if (source.getArticleVisibility() != null) target.setArticleVisibility(source.getArticleVisibility());
        if (source.getLikesHidden() != null) target.setLikesHidden(source.getLikesHidden());
        if (source.getFavoritesHidden() != null) target.setFavoritesHidden(source.getFavoritesHidden());
        if (source.getFollowListHidden() != null) target.setFollowListHidden(source.getFollowListHidden());
        if (source.getFollowersListHidden() != null) target.setFollowersListHidden(source.getFollowersListHidden());
        if (source.getAllowPrivateMessage() != null) target.setAllowPrivateMessage(source.getAllowPrivateMessage());
        if (source.getAllowMention() != null) target.setAllowMention(source.getAllowMention());
        if (source.getNewFollowerNotification() != null) target.setNewFollowerNotification(source.getNewFollowerNotification());
        if (source.getAllowRecommendation() != null) target.setAllowRecommendation(source.getAllowRecommendation());
        if (source.getInterestBasedRecommendation() != null) target.setInterestBasedRecommendation(source.getInterestBasedRecommendation());
        if (source.getDataAnalysis() != null) target.setDataAnalysis(source.getDataAnalysis());
        if (source.getThirdPartyDataSharing() != null) target.setThirdPartyDataSharing(source.getThirdPartyDataSharing());
    }

    private void fillDefaultPrivacySetting(UserPrivacy userPrivacy) {
        userPrivacy.setEmailVisibility("0");
        userPrivacy.setPhoneVisibility("0");
        userPrivacy.setProfileVisibility("0");
        userPrivacy.setCanComment("0");
        userPrivacy.setArticleVisibility("0");
        userPrivacy.setLikesHidden("0");
        userPrivacy.setFavoritesHidden("0");
        userPrivacy.setFollowListHidden("0");
        userPrivacy.setFollowersListHidden("0");
        userPrivacy.setAllowPrivateMessage("0");
        userPrivacy.setAllowMention("0");
        userPrivacy.setNewFollowerNotification("0");
        userPrivacy.setAllowRecommendation("0");
        userPrivacy.setInterestBasedRecommendation("0");
        userPrivacy.setDataAnalysis("0");
        userPrivacy.setThirdPartyDataSharing("0");
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishArticle(Article articleVO) {
        log.info("UserServiceImpl.publishArticle开始发布文章");
        int insert = 0;
        String lockKey = "pub:art:key:"+articleVO.getUserId();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, articleVO.getTitle(), 30, TimeUnit.SECONDS);
        if(Boolean.FALSE.equals(locked)){
            return false;
        }
        try {
            articleVO.setStatus("2");
            insert = articlemapper.insert(articleVO);

            // 2. 创建审核记录 默认人工审核
            auditRecordService.createAuditRecord(articleVO.getId());

            // TODO 自动审核 3. 立即执行自动审核（同步）
            //auditRecordService.autoAudit(articleVO);
        } catch (Exception e) {
            log.error("UserServiceImpl.publishArticle发布文章异常",e);
            throw new RuntimeException(e);
        } finally {
            redisTemplate.delete(lockKey);
        }

        return insert > 0;
    }

    @Override
    public Boolean updateArticle(Article articleVO) {
        log.info("UserServiceImpl.publishArticle开始修改文章");
        int i = 0;
        try {
            articleVO.setContent(articleVO.getContent());
            articleVO.setStatus("2");
            articleVO.setUpdatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
            articleVO.setPublishedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
            i = articlemapper.updateById(articleVO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("UserServiceImpl.updateArticle结束修改文章,i={}",i);
        return i > 0;
    }



    /**
     * 查活跃用户
     * @param days
     * @param size
     * @return
     */
    @Override
    public List<UserVO> queryActiveUser(Integer days, Integer size) {
        if(null == days || days <= 0){
            days = 7;
        }
        if(null == size || size <= 0){
            size = 10;
        }
        return baseMapper.selectActiveUser(days,size);
    }



    /**
     * 注册 校验验证码 密码 邮箱
     * @param registerVO
     * @return
     */
    @Override
    public Result register(RegisterVO registerVO) {
        if (null == registerVO) {
            return Result.fail(EnumReturn.USERNAME_PASSWORD_EMPTY);
        }

        String phoneNumber = registerVO.getPhoneNumber();
        String email = registerVO.getEmail();
        boolean phoneRegister = StringUtils.isNotBlank(phoneNumber);

        if (phoneRegister) {
            if (!Boolean.TRUE.equals(aliyunSmsProperties.getEnabled())) {
                return Result.fail(400, "短信服务未启用");
            }
            if (StringUtils.isBlank(registerVO.getCode())) {
                return Result.fail(400, "短信验证码不能为空");
            }
            LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
            phoneQuery.eq(User::getPhone, phoneNumber);
            if (this.getOne(phoneQuery) != null) {
                return Result.fail(400, "手机号已被注册");
            }
            if (!checkSmsCodeFromRedis(phoneNumber, registerVO.getCode(), true)) {
                return Result.fail(400, "短信验证码错误或已过期");
            }
            email = null;
        } else {
            if (StrUtil.isEmpty(email)) {
                return Result.fail(EnumReturn.EMAIL_NOT_EXISTS);
            }

            String verificationCode = (String) redisTemplate.opsForValue()
                    .get(RedisVerificationKey.getVerificationCodeKey(email));

            if (verificationCode == null || !verificationCode.equals(registerVO.getCode())) {
                return Result.fail(EnumReturn.VERIFICATION_CODE_ERROR);
            }
        }

        String encryptSeed = StringUtils.isNotBlank(email) ? email : phoneNumber;
        String encryptPwd = UserUtil.getUserEncryptPassword(encryptSeed, registerVO.getPassword());

        User user = new User();
        user.setEmail(email);
        user.setPhone(phoneNumber);
        user.setPhoneVerified(phoneRegister ? "1" : "0");
        user.setEmailVerified(phoneRegister ? "0" : "1");
        user.setUsername(registerVO.getUsername());
        user.setPassword(encryptPwd);
        user.setAvatar("http://127.0.0.1:9000/website/908470.jpg");

        if(this.save(user)){
            initUserPrivacyIfAbsent(user.getId());
            UserRole userRole = createDefaultUserRole(user.getId());
            if (userRole == null) {
                log.warn("注册用户默认角色初始化失败 userId={}", user.getId());
            }

            // 创建注册通知
            try {
                Notification notification = new Notification();
                notification.setUserId(user.getId());
                notification.setFromUserId(user.getId());
                notification.setType("system");
                notification.setActionType("user_register");
                notification.setTitle("新用户注册");
                notification.setContent(user.getUsername() + " 注册了账号");
                notification.setTargetType("user");
                notification.setTargetId(user.getId());
                notification.setIsRead("1");
                notification.setIsDeleted("0");
                notification.setNotifyLevel("normal");
                notification.setCreatedAt(new Date());
                notificationService.save(notification);
            } catch (Exception e) {
                log.warn("创建注册通知失败: {}", e.getMessage());
            }
            
            StpUtil.login(user.getId());
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

            tokenInfo.setTokenTimeout(3600);
            SaSession session = StpUtil.getSession();
            session.set("USER_NAME",user.getUsername());
            session.set("USER_ID",user.getId());
            session.set("USER_ROLE", userRole == null || StringUtils.isBlank(userRole.getRoleName()) ? "user" : userRole.getRoleName());
            return Result.success(tokenInfo);
        }else{
            return Result.fail(EnumReturn.REGISTER_FAIL);
        }
    }







    @Override
    @Schema(description = "注册发送")
    public Result sendVerificationCode(RegisterVO registerVO) {
        if (registerVO == null || StringUtils.isBlank(registerVO.getEmail())) {
            return Result.fail(EnumReturn.EMAIL_IS_EMPTY);
        }

        if (Boolean.TRUE.equals(tencentCaptchaProperties.getEnabled())) {
            if (StringUtils.isBlank(registerVO.getCaptchaTicket()) || StringUtils.isBlank(registerVO.getCaptchaRandStr())) {
                return Result.fail(400, "请先完成滑动验证码校验");
            }
            if (!verifyTencentCaptcha(registerVO.getCaptchaTicket(), registerVO.getCaptchaRandStr())) {
                return Result.fail(400, "滑动验证码校验失败");
            }
        } else {
            if (StringUtils.isBlank(registerVO.getCaptchaId()) || StringUtils.isBlank(registerVO.getCaptchaCode())) {
                return Result.fail(400, "请先完成图形验证码校验");
            }
            String captchaKey = RedisVerificationKey.getRegisterCaptchaKey(registerVO.getCaptchaId());
            String cacheCaptcha = (String) redisTemplate.opsForValue().get(captchaKey);
            if (StringUtils.isBlank(cacheCaptcha)
                    || !StringUtils.equalsIgnoreCase(cacheCaptcha, registerVO.getCaptchaCode().trim())) {
                return Result.fail(400, "图形验证码错误或已过期");
            }
            redisTemplate.delete(captchaKey);
        }

        String email = registerVO.getEmail();
        //邮箱已被注册过
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail,email);
        if(null != this.getOne(queryWrapper)){
            return Result.fail(EnumReturn.EMAIL_ALREADY_REGISTERED);
        }

        String verificationCode = RandomUtil.randomNumbers(6);
        String vCodeKey = RedisVerificationKey.getVerificationCodeKey(email);


        //检查是否频繁
        Long expire = redisTemplate.getExpire(vCodeKey);
        if(expire != null && expire >= 240){
            return Result.fail(EnumReturn.VERIFICATION_CODE_FREQUENT);
        }

        //保存到redis
        redisTemplate.opsForValue().set(vCodeKey ,verificationCode,5, TimeUnit.MINUTES);



        //发送邮件
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html>")
                .append("<body>")
                .append("<h2 style='color: #4CAF50;'>欢迎来到程序员社区！</h2>")
                .append("<p>亲爱的用户，</p>")
                .append("<p>您的注册验证码是：<strong style='font-size: 24px; color: #FF5722;'>")
                .append(verificationCode)
                .append("</strong></p>")
                .append("<p>此验证码有效期为 5 分钟，请及时使用。</p>")
                .append("<p style='margin-top: 20px;'>祝您使用愉快！</p>")
                .append("<p>程序员社区团队</p>")
                .append("</body>")
                .append("</html>");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("yanjue2024@163.com");
        message.setSubject("欢迎注册 - 验证码");
        message.setTo(email);
        message.setText(emailContent.toString());  // 发送 HTML 格式的内容

        log.info("################## 验证码：{}",verificationCode);

        try {
            javaMailSender.send(message);
            return Result.success("邮件成功发送");
        } catch (Exception e) {
            log.error(" 邮件发送失败：{},邮箱：{}",e.getMessage(),email);
            return Result.fail(EnumReturn.VERIFICATION_CODE_ERROR);
        }

    }







    @Override
    public Result updateUsername(Integer userId, UpdateUsernameVO updateUsernameVO) {
        User user = this.getById(userId);
        user.setUsername(updateUsernameVO.getUsername());
        if(this.updateById(user)){
            return Result.success(EnumReturn.OPERATION_SUCCESS);
        }
        return Result.fail(EnumReturn.OPERATION_FAIL);
    }







    @Override
    public Result updatePassword(Long userId, UpdatePasswordVO updatePasswordVO) {
        User user = this.getById(userId);
        String oldPassword = updatePasswordVO.getOldPassword();
        String newPassword = updatePasswordVO.getNewPassword();
        String preferredSeed = StringUtils.defaultIfBlank(user.getEmail(), user.getPhone());
        if (!matchesPasswordByAnySeed(user, oldPassword)) {
            return  Result.fail(EnumReturn.PASSWORD_ERROR);
        }

        //新密码加密
        String newEncryptPassword = UserUtil.getUserEncryptPassword(preferredSeed, newPassword);
        user.setPassword(newEncryptPassword);
        user.setUpdatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
        if(this.updateById(user)){
            return Result.success(EnumReturn.OPERATION_SUCCESS);
        }
        return Result.fail(EnumReturn.OPERATION_FAIL);
    }







    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result forgetPassword(ForgetPasswordVO forgetPasswordVO) {
//        String email = forgetPasswordVO.getEmail();
//        String verificationCode = forgetPasswordVO.getVerificationCode();
//        String newPassword = forgetPasswordVO.getNewPassword();
//
//        //从redis中获取验证码
//        String redisVerificationCode = (String) redisTemplate.opsForValue()
//                .get(RedisVerificationKey.getVerificationCodeKey(email));
//
//        if(redisVerificationCode == null || !redisVerificationCode.equals(verificationCode)){
//            return Result.fail(EnumReturn.VERIFICATION_CODE_ERROR);
//        }
//        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(User::getEmail,email);
//        User user = this.getOne(queryWrapper);
//        if(user == null){
//            return Result.fail(EnumReturn.EMAIL_NOT_EXISTS);
//        }
//
//        String userEncryptPassword = UserUtil.getUserEncryptPassword(user.getAccount(), newPassword);
//        user.setPassword(userEncryptPassword);
//        if(this.updateById(user)){
//            return Result.success(EnumReturn.OPERATION_SUCCESS);
//        }
        return Result.fail(EnumReturn.OPERATION_FAIL);
    }





    @Override
    public Result login(UserLoginVO userLoginVO) {
        if (null == userLoginVO) {
            return Result.fail(EnumReturn.USERNAME_PASSWORD_EMPTY);
        }

        if ("phone_code".equalsIgnoreCase(StringUtils.trimToEmpty(userLoginVO.getLoginType()))) {
            PhoneLoginRequest phoneLoginRequest = new PhoneLoginRequest();
            phoneLoginRequest.setPhoneNumber(StringUtils.defaultIfBlank(userLoginVO.getPhoneNumber(), userLoginVO.getCondition()));
            phoneLoginRequest.setCode(userLoginVO.getCode());
            phoneLoginRequest.setRememberMe(Boolean.TRUE.equals(userLoginVO.getRememberMe()));
            return loginByPhoneCode(phoneLoginRequest);
        }

        String condition = userLoginVO.getCondition();
        if(StringUtils.isEmpty(condition)){
            return Result.fail(EnumReturn.USERNAME_EMAIL_EMPTY);
        }
        if (StringUtils.isBlank(userLoginVO.getPassword())) {
            return Result.fail(EnumReturn.USERNAME_PASSWORD_EMPTY);
        }
        List<User> userList = userMapper.selectByCondition(condition);
        if(CollectionUtils.isEmpty(userList)){
            return Result.fail(EnumReturn.ACCOUNT_NOT_EXISTS);
        }
        User user = userList.get(0);

        if(!"0".equals(user.getStatus())){
            return Result.fail(EnumReturn.USER_DISABLED);
        }


        if(!matchesPasswordByAnySeed(user, userLoginVO.getPassword())){
            return Result.fail(EnumReturn.PASSWORD_ERROR);
        }
        migratePasswordToPreferredSeedIfNeeded(user, userLoginVO.getPassword());

        //查询角色
        LambdaQueryWrapper<UserRole> l = new LambdaQueryWrapper<>();
        l.eq(UserRole::getUserId,user.getId());
        UserRole userRole = userRolemapper.selectOne(l);
        if(null == userRole){
            userRole = createDefaultUserRole(user.getId());
            if (userRole == null) {
                return Result.fail(400, "用户角色初始化失败");
            }
        }
        String code = "user";
        if(null != userRole.getRoleName()){
            code = userRole.getRoleName();
        }

        user.setLastLoginTime(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
        baseMapper.updateById(user);
        //登录成功，记录token
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        tokenInfo.setTokenTimeout(3600);
        SaSession session = StpUtil.getSession();
        session.set("USER_ID",user.getId());
        session.set("USER_ROLE",code);

        if(Boolean.TRUE.equals(userLoginVO.getRememberMe())){
            tokenInfo.setTokenTimeout(3600 * 24 * 7);
        }
        // 构建登录响应
        LoginResponseVO loginResponse = new LoginResponseVO();
        loginResponse.setTokenInfo(tokenInfo);
        loginResponse.setUserId(user.getId());
        loginResponse.setUsername(user.getUsername());
        loginResponse.setNickname(user.getNickname());
        loginResponse.setAvatar(user.getAvatar());
        loginResponse.setRoles(code);


        //保存登录时间和登录ip TODO
        user.setUpdatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
        this.updateById(user);
        
        // 创建登录通知
        try {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setFromUserId(user.getId());
            notification.setType("system");
            notification.setActionType("user_login");
            notification.setTitle("用户登录");
            notification.setContent(user.getUsername() + " 登录了系统");
            notification.setTargetType("user");
            notification.setTargetId(user.getId());
            notification.setIsRead("0");
            notification.setIsDeleted("0");
            notification.setNotifyLevel("normal");
            notification.setCreatedAt(new Date());
            notificationService.save(notification);
        } catch (Exception e) {
            log.warn("创建登录通知失败: {}", e.getMessage());
        }
        
        return Result.success(loginResponse);
    }

    @Override
    public Result loginByPhoneCode(PhoneLoginRequest phoneLoginRequest) {
        if (phoneLoginRequest == null
                || StringUtils.isAnyBlank(phoneLoginRequest.getPhoneNumber(), phoneLoginRequest.getCode())) {
            return Result.fail(400, "手机号或验证码不能为空");
        }
        if (!Boolean.TRUE.equals(aliyunSmsProperties.getEnabled())) {
            return Result.fail(400, "短信服务未启用");
        }
        if (!checkSmsCodeFromRedis(phoneLoginRequest.getPhoneNumber(), phoneLoginRequest.getCode(), true)) {
            return Result.fail(400, "短信验证码错误或已过期");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phoneLoginRequest.getPhoneNumber());
        User user = this.getOne(queryWrapper);
        if (user == null) {
            user = autoRegisterPhoneUser(phoneLoginRequest.getPhoneNumber());
            if (user == null) {
                return Result.fail(EnumReturn.REGISTER_FAIL);
            }
        }
        if (!"0".equals(user.getStatus())) {
            return Result.fail(EnumReturn.USER_DISABLED);
        }

        LambdaQueryWrapper<UserRole> l = new LambdaQueryWrapper<>();
        l.eq(UserRole::getUserId, user.getId());
        UserRole userRole = userRolemapper.selectOne(l);
        if (null == userRole) {
            userRole = createDefaultUserRole(user.getId());
            if (userRole == null) {
                return Result.fail(400, "用户角色初始化失败");
            }
        }
        String code = userRole.getRoleName() == null ? "user" : userRole.getRoleName();

        user.setLastLoginTime(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
        user.setUpdatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
        baseMapper.updateById(user);

        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        tokenInfo.setTokenTimeout(3600);
        if (Boolean.TRUE.equals(phoneLoginRequest.getRememberMe())) {
            tokenInfo.setTokenTimeout(3600 * 24 * 7);
        }
        SaSession session = StpUtil.getSession();
        session.set("USER_ID", user.getId());
        session.set("USER_ROLE", code);

        LoginResponseVO loginResponse = new LoginResponseVO();
        loginResponse.setTokenInfo(tokenInfo);
        loginResponse.setUserId(user.getId());
        loginResponse.setUsername(user.getUsername());
        loginResponse.setNickname(user.getNickname());
        loginResponse.setAvatar(user.getAvatar());
        loginResponse.setRoles(code);
        return Result.success(loginResponse);
    }

    @Override
    public Result checkUsername(RegisterVO request) {

        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        q.eq(User::getUsername,request.getUsername());
        Long l = userMapper.selectCount(q);
        System.out.println(l);
        return l > 0 ? Result.fail(EnumReturn.USERNAME_EXITS) : Result.success(true);
    }


    @Override
    @Schema(description = "发送邮箱验证码")
    public Result sendCode(EmailRequest emailRequest) {
        String email = emailRequest.getEmail();

        String verificationCode = RandomUtil.randomNumbers(6);
        String vCodeKey = RedisVerificationKey.getVerificationCodeKey(email);

        //检查是否频繁
        Long expire = redisTemplate.getExpire(vCodeKey);
        if(expire != null && expire >= 240){
            return Result.fail(EnumReturn.VERIFICATION_CODE_FREQUENT);
        }

        //保存到redis
        redisTemplate.opsForValue().set(vCodeKey ,verificationCode,5, TimeUnit.MINUTES);

        String emailContent = generateEmailContent(emailRequest.getPurpose(), verificationCode);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true表示multipart

            helper.setFrom("yanjue2024@163.com");
            helper.setTo(email);
            helper.setSubject(emailRequest.getPurpose().equals("changeEmail") ? "邮箱更改 - 验证码" : "密码更改 - 验证码");
            helper.setText(emailContent, true); // 第二个参数表示内容为HTML

            javaMailSender.send(message);
            return Result.success("邮件成功发送");
        } catch (Exception e) {
            log.error(" 邮件发送失败：{},邮箱：{}", e.getMessage(), email);
            return Result.fail(EnumReturn.EMAIL_SEND_FAIL);
        }
    }

    @Override
    public Result sendSmsCode(SmsCodeSendRequest smsCodeSendRequest) {
        if (smsCodeSendRequest == null || StringUtils.isBlank(smsCodeSendRequest.getPhoneNumber())) {
            return Result.fail(400, "手机号不能为空");
        }
        if (!Boolean.TRUE.equals(aliyunSmsProperties.getEnabled())) {
            return Result.fail(400, "短信服务未启用");
        }
        if (StringUtils.isAnyBlank(aliyunSmsProperties.getSchemeName(), aliyunSmsProperties.getSignName(), aliyunSmsProperties.getTemplateCode())) {
            return Result.fail(400, "短信服务配置不完整");
        }

        String outId = UUID.randomUUID().toString().replace("-", "");
        long validTimeSeconds = aliyunSmsProperties.getValidTime() == null ? 300L : aliyunSmsProperties.getValidTime();
        long intervalSeconds = aliyunSmsProperties.getInterval() == null ? 60L : aliyunSmsProperties.getInterval();
        String smsCodeKey = RedisVerificationKey.getSmsCodeKey(smsCodeSendRequest.getPhoneNumber());
        Long expire = redisTemplate.getExpire(smsCodeKey);
        if (expire != null && expire > Math.max(0L, validTimeSeconds - intervalSeconds)) {
            return Result.fail(400, "短信验证码发送过于频繁");
        }

        long validMinutes = Math.max(1L, validTimeSeconds / 60L);
        String templateParam = String.format("{\"code\":\"##code##\",\"min\":\"%d\"}", validMinutes);
        try (AsyncClient client = createAliyunSmsClient()) {
            SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
                    .schemeName(aliyunSmsProperties.getSchemeName())
                    .countryCode(aliyunSmsProperties.getCountryCode())
                    .phoneNumber(smsCodeSendRequest.getPhoneNumber())
                    .signName(aliyunSmsProperties.getSignName())
                    .templateCode(aliyunSmsProperties.getTemplateCode())
                    .templateParam(templateParam)
                    .validTime(aliyunSmsProperties.getValidTime())
                    .codeLength(aliyunSmsProperties.getCodeLength())
                    .interval(aliyunSmsProperties.getInterval())
                    .duplicatePolicy(aliyunSmsProperties.getDuplicatePolicy())
                    .codeType(aliyunSmsProperties.getCodeType())
                    .returnVerifyCode(true)
                    .outId(outId)
                    .build();

            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCode(request).get();
            SendSmsVerifyCodeResponseBody body = response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.getSuccess())) {
                log.warn("阿里云短信发送失败 code={}, message={}",
                        body == null ? null : body.getCode(),
                        body == null ? null : body.getMessage());
                return Result.fail(400, "短信验证码发送失败");
            }
            SendSmsVerifyCodeResponseBody.Model model = body.getModel();
            String responseOutId = model == null ? outId : StringUtils.defaultIfBlank(model.getOutId(), outId);
            String verifyCode = model == null ? "" : model.getVerifyCode();
            if (StringUtils.isBlank(verifyCode)) {
                log.error("阿里云短信发送成功但未返回验证码，无法写入Redis。请确认当前短信API支持ReturnVerifyCode");
                return Result.fail(400, "短信验证码发送异常，请稍后重试");
            }
            redisTemplate.opsForValue().set(smsCodeKey, verifyCode, validTimeSeconds, TimeUnit.SECONDS);

            Map<String, Object> data = new HashMap<>();
            data.put("requestId", model == null ? body.getRequestId() : StringUtils.defaultIfBlank(body.getRequestId(), model.getRequestId()));
            data.put("bizId", model == null ? "" : StringUtils.defaultString(model.getBizId()));
            data.put("outId", responseOutId);
            data.put("expireSeconds", validTimeSeconds);
            return Result.success(data);
        } catch (Exception e) {
            log.error("阿里云短信发送异常", e);
            String detail = StringUtils.defaultIfBlank(e.getMessage(), "unknown");
            return Result.fail(400, "短信验证码发送异常: " + detail);
        }
    }

    @Override
    public Result verifySmsCode(SmsCodeVerifyRequest smsCodeVerifyRequest) {
        if (smsCodeVerifyRequest == null
                || StringUtils.isAnyBlank(smsCodeVerifyRequest.getPhoneNumber(), smsCodeVerifyRequest.getCode())) {
            return Result.fail(400, "手机号或验证码不能为空");
        }
        if (!Boolean.TRUE.equals(aliyunSmsProperties.getEnabled())) {
            return Result.fail(400, "短信服务未启用");
        }

        boolean verified = checkSmsCodeFromRedis(smsCodeVerifyRequest.getPhoneNumber(), smsCodeVerifyRequest.getCode(), false);
        return verified ? Result.success(true) : Result.fail(400, "短信验证码错误或已过期");
    }



    // 生成邮件内容的函数
    private String generateEmailContent(String purpose, String verificationCode) {
        StringBuilder content = new StringBuilder();
        content.append("<html>")
                .append("<body>")
                .append("<h2 style='color: #4CAF50;'>验证码确认</h2>")
                .append("<p>亲爱的用户，</p>")
                .append("<p>您已请求").append(purpose.equals("changeEmail") ? "更改您的注册邮箱" : "重置您的密码").append("。</p>")
                .append("<p>请使用以下验证码确认您的操作：<strong style='font-size: 24px; color: #FF5722;'>")
                .append(verificationCode)
                .append("</strong></p>")
                .append("<p>此验证码有效期为 5 分钟，请及时使用。</p>")
                .append("<p>如果您没有进行此操作，请忽略此邮件。</p>")
                .append("<p style='margin-top: 20px;'>感谢您的配合！</p>")
                .append("<p>程序员社区团队</p>")
                .append("</body>")
                .append("</html>");
        return content.toString();
    }

    private AsyncClient createAliyunSmsClient() {
        return AsyncClient.builder()
                .region(aliyunSmsProperties.getRegion())
                .credentialsProvider(EnvironmentVariableCredentialProvider.create())
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride(aliyunSmsProperties.getEndpoint())
                )
                .build();
    }

    private boolean matchesPasswordByAnySeed(User user, String rawPassword) {
        if (user == null || StringUtils.isBlank(rawPassword) || StringUtils.isBlank(user.getPassword())) {
            return false;
        }
        String password = user.getPassword();
        if (StringUtils.isNotBlank(user.getEmail())) {
            String emailHash = UserUtil.getUserEncryptPassword(user.getEmail(), rawPassword);
            if (StringUtils.equals(password, emailHash)) {
                return true;
            }
        }
        if (StringUtils.isNotBlank(user.getPhone())) {
            String phoneHash = UserUtil.getUserEncryptPassword(user.getPhone(), rawPassword);
            if (StringUtils.equals(password, phoneHash)) {
                return true;
            }
        }
        return false;
    }

    private void migratePasswordToPreferredSeedIfNeeded(User user, String rawPassword) {
        if (user == null || StringUtils.isBlank(rawPassword) || StringUtils.isBlank(user.getPassword())) {
            return;
        }
        String preferredSeed = StringUtils.defaultIfBlank(user.getEmail(), user.getPhone());
        if (StringUtils.isBlank(preferredSeed)) {
            return;
        }
        String preferredHash = UserUtil.getUserEncryptPassword(preferredSeed, rawPassword);
        if (StringUtils.equals(user.getPassword(), preferredHash)) {
            return;
        }
        user.setPassword(preferredHash);
        user.setUpdatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
        this.updateById(user);
    }

    private boolean checkSmsCodeFromRedis(String phoneNumber, String verifyCode, boolean deleteAfterSuccess) {
        if (StringUtils.isAnyBlank(phoneNumber, verifyCode)) {
            return false;
        }
        String smsCodeKey = RedisVerificationKey.getSmsCodeKey(phoneNumber);
        String cachedCode = (String) redisTemplate.opsForValue().get(smsCodeKey);
        if (!StringUtils.equals(cachedCode, verifyCode.trim())) {
            return false;
        }
        if (deleteAfterSuccess) {
            redisTemplate.delete(smsCodeKey);
        }
        return true;
    }

    private User autoRegisterPhoneUser(String phoneNumber) {
        Date now = Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant());
        String suffix = phoneNumber.length() >= 4 ? phoneNumber.substring(phoneNumber.length() - 4) : phoneNumber;
        String username = "u" + suffix + RandomUtil.randomNumbers(4);
        String unavailablePassword = UserUtil.getUserEncryptPassword(phoneNumber, UUID.randomUUID().toString());

        User user = new User();
        user.setPhone(phoneNumber);
        user.setUsername(username);
        user.setNickname(username);
        user.setPassword(unavailablePassword);
        user.setStatus("0");
        user.setPhoneVerified("1");
        user.setEmailVerified("0");
        user.setAvatar("http://127.0.0.1:9000/website/908470.jpg");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        if (!this.save(user)) {
            return null;
        }
        initUserPrivacyIfAbsent(user.getId());
        if (createDefaultUserRole(user.getId()) == null) {
            return null;
        }
        return user;
    }

    private UserRole createDefaultUserRole(Long userId) {
        if (userId == null) {
            return null;
        }
        LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(Role::getRoleCode, "user")
                .or()
                .eq(Role::getRoleName, "user")
                .last("limit 1");
        Role role = rolemapper.selectOne(roleWrapper);

        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleName(role == null ? "user" : role.getRoleCode());
        userRole.setRoleId(role == null ? null : role.getId());
        userRole.setCreatedAt(new Date());
        int inserted = userRolemapper.insert(userRole);
        return inserted > 0 ? userRole : null;
    }

    private boolean verifyTencentCaptcha(String ticket, String randStr) {
        if (StringUtils.isAnyBlank(tencentCaptchaProperties.getAppId(), tencentCaptchaProperties.getAppSecretKey())) {
            log.error("腾讯验证码未正确配置: appId/appSecretKey 缺失");
            return false;
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(tencentCaptchaProperties.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(tencentCaptchaProperties.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(tencentCaptchaProperties.getReadTimeoutSeconds(), TimeUnit.SECONDS)
                .build();

        FormBody.Builder bodyBuilder = new FormBody.Builder()
                .add("aid", tencentCaptchaProperties.getAppId())
                .add("AppSecretKey", tencentCaptchaProperties.getAppSecretKey())
                .add("Ticket", ticket)
                .add("Randstr", randStr);

        String clientIp = resolveClientIp();
        if (shouldSendTencentUserIp(clientIp)) {
            bodyBuilder.add("UserIP", clientIp);
        } else {
            log.info("腾讯验证码校验跳过UserIP，当前IP不适合上送: {}", clientIp);
        }

        FormBody body = bodyBuilder.build();

        Request request = new Request.Builder()
                .url(tencentCaptchaProperties.getVerifyUrl())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("腾讯验证码校验HTTP失败 status={}", response.code());
                return false;
            }
            String responseText = response.body() == null ? "" : response.body().string();
            JsonNode root = objectMapper.readTree(responseText);
            int verifyResponse = root.path("response").asInt(0);
            if (verifyResponse != 1) {
                log.warn("腾讯验证码校验失败 resp={}", responseText);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("腾讯验证码校验异常", e);
            return false;
        }
    }

    private String resolveClientIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            return "127.0.0.1";
        }
        HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(realIp)) {
            return realIp;
        }
        String remoteAddr = request.getRemoteAddr();
        return StringUtils.isBlank(remoteAddr) ? "127.0.0.1" : remoteAddr;
    }

    private boolean shouldSendTencentUserIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        String normalized = ip.trim();
        if ("127.0.0.1".equals(normalized)
                || "::1".equals(normalized)
                || "0:0:0:0:0:0:0:1".equals(normalized)) {
            return false;
        }
        if (normalized.startsWith("10.") || normalized.startsWith("192.168.")) {
            return false;
        }
        String[] parts = normalized.split("\\.");
        if (parts.length == 4 && "172".equals(parts[0])) {
            try {
                int second = Integer.parseInt(parts[1]);
                if (second >= 16 && second <= 31) {
                    return false;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return true;
    }

    private void initUserPrivacyIfAbsent(Long userId) {
        if (userId == null) {
            return;
        }
        LambdaQueryWrapper<UserPrivacy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPrivacy::getUserId, userId);
        if (userPrivacyService.count(wrapper) > 0) {
            return;
        }

        Date now = new Date();
        UserPrivacy userPrivacy = new UserPrivacy();
        userPrivacy.setUserId(userId);
        fillDefaultPrivacySetting(userPrivacy);
        userPrivacy.setCreatedAt(now);
        userPrivacy.setUpdatedAt(now);
        userPrivacyService.save(userPrivacy);
    }

    /**
     * 引导式注册：初始化用户兴趣标签画像
     * 将用户选择的标签写入 user_profile，解决推荐冷启动问题
     */
    @Override
    @Transactional
    public Result initInterestTags(InitInterestTagsRequest request) {
        if (request == null || request.getUserId() == null || CollectionUtils.isEmpty(request.getTagIds())) {
            return Result.fail(400, "参数错误，请至少选择一个标签");
        }

        Long userId = request.getUserId();
        List<Long> tagIds = request.getTagIds().stream().distinct().limit(20).collect(Collectors.toList());

        // 查询选中标签的详情（包括 categoryId）
        List<ArticleTag> tags = articleTagMapper.selectBatchIds(tagIds);
        if (CollectionUtils.isEmpty(tags)) {
            return Result.fail(400, "未找到有效标签");
        }

        // 构建 tagProfile / categoryProfile / tagLastTimeMap
        Map<Long, Double> tagProfile = new HashMap<>();
        Map<Long, Double> categoryProfile = new HashMap<>();
        Map<Long, Long> tagLastTimeMap = new HashMap<>();
        long now = System.currentTimeMillis();
        double initWeight = 5.0; // 初始权重，相当于 2~3 次 LIKE 行为

        for (ArticleTag tag : tags) {
            tagProfile.put(tag.getId(), initWeight);
            tagLastTimeMap.put(tag.getId(), now);
            if (tag.getCategoryId() != null) {
                categoryProfile.merge(tag.getCategoryId(), initWeight, Double::sum);
            }
        }

        // Upsert user_profile
        UserProfile existing = userProfileMapper.selectByUserId(userId);
        UserProfile profile = existing != null ? existing : new UserProfile();
        profile.setUserId(userId);
        profile.setTagProfile(tagProfile);
        profile.setCategoryProfile(categoryProfile);
        profile.setTagLastTimeMap(tagLastTimeMap);
        if (profile.getAuthorProfile() == null) {
            profile.setAuthorProfile(new HashMap<>());
        }
        if (profile.getBehaviorCount() == null) {
            profile.setBehaviorCount(0);
        }
        // 关键：把 profileLevel 提到 1，让推荐算法走内容召回而非降级到热榜
        profile.setProfileLevel(1);
        profile.setLastCalculatedAt(LocalDateTime.now());

        if (existing == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }

        log.info("用户[{}]完成兴趣标签初始化，选中标签数={}", userId, tags.size());
        return Result.success(true);
    }

}




