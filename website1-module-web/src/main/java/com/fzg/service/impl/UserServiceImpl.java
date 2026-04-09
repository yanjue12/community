package com.fzg.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.constant.RedisVerificationKey;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.UserRolemapper;
import com.fzg.model.*;
import com.fzg.service.AuditRecordService;
import com.fzg.service.INotificationService;
import com.fzg.service.UserPrivacyService;
import com.fzg.service.UserService;
import com.fzg.mapper.UserMapper;
import com.fzg.util.UserUtil;
import com.fzg.vo.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private Articlemapper articlemapper;
    @Autowired
    private UserRolemapper userRolemapper;
    @Autowired
    private UserPrivacyService userPrivacyService;
    @Autowired
    private AuditRecordService auditRecordService;
    @Autowired
    private INotificationService notificationService;


    /**
     * 用户修改隐私，可以单独文章改，也可以个人设置所有作品私密等。
     * @param upPriSetVO
     * @return
     */
    @Override
    public Boolean updatePrivateSetting(UpdatePrivateSettingVO upPriSetVO) {
        log.info("UserServiceImpl.updatePrivateSetting开始修改隐私设置");

        Boolean b = false;
        //懒得搞字典，直接弄 ALL为全局设置
        if("ALL".equals(upPriSetVO.getFlag())){
            b = userPrivacyService.updateById(upPriSetVO);
        }else if("MONOMER".equals(upPriSetVO.getFlag())){
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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishArticle(Article articleVO) {
        log.info("UserServiceImpl.publishArticle开始发布文章");
        int insert = 0;
        String lockKey = "pub:art:key:"+articleVO.getUserId();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, articleVO.getTitle(), 30, TimeUnit.SECONDS);
        if(locked){
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

        String email = registerVO.getEmail();
        if(StrUtil.isEmpty(email)){
            return Result.fail(EnumReturn.EMAIL_NOT_EXISTS);
        }

        //从redis中获取验证码
        String verificationCode = (String) redisTemplate.opsForValue()
                .get(RedisVerificationKey.getVerificationCodeKey(email));

        if(verificationCode == null || !verificationCode.equals(registerVO.getCode())){
            return Result.fail(EnumReturn.VERIFICATION_CODE_ERROR);
        }

        //密码加密
        String encryptPwd = UserUtil.getUserEncryptPassword(email, registerVO.getPassword());

        User user = new User();
        user.setEmail(email);
        user.setUsername(registerVO.getUsername());
        user.setPassword(encryptPwd);
        user.setAvatar("http://127.0.0.1:9000/website/908470.jpg");

        if(this.save(user)){
            initUserPrivacyIfAbsent(user.getId());

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
            return Result.success(tokenInfo);
        }else{
            return Result.fail(EnumReturn.REGISTER_FAIL);
        }
    }







    @Override
    @Schema(description = "注册发送")
    public Result sendVerificationCode(RegisterVO registerVO) {
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
        //密码加密之后验证。
        String userOldEncryptPassword = UserUtil.getUserEncryptPassword(user.getEmail(), oldPassword);

        if(!user.getPassword().equals(userOldEncryptPassword)){
            return  Result.fail(EnumReturn.PASSWORD_ERROR);
        }

        //新密码加密
        String newEncryptPassword = UserUtil.getUserEncryptPassword(user.getEmail(), newPassword);
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

        String condition = userLoginVO.getCondition();
        if(StringUtils.isEmpty(condition)){
            return Result.fail(EnumReturn.USERNAME_EMAIL_EMPTY);
        }
        List<User> userList = userMapper.selectByCondition(condition);
        if(CollectionUtils.isEmpty(userList)){
            Result r = new Result<>();
            r.setMsg("当前用户名不存在，请重新输入");
        }
        User user = userList.get(0);

        if(!"0".equals(user.getStatus())){
            return Result.fail(EnumReturn.USER_DISABLED);
        }


        String encryptPwd = UserUtil.getUserEncryptPassword(user.getEmail(), userLoginVO.getPassword());
        if(!user.getPassword().equals(encryptPwd)){
            return Result.fail(EnumReturn.PASSWORD_ERROR);
        }

        //查询角色
        LambdaQueryWrapper<UserRole> l = new LambdaQueryWrapper<>();
        l.eq(UserRole::getUserId,user.getId());
        UserRole userRole = userRolemapper.selectOne(l);
        if(null == userRole){
            return Result.fail(EnumReturn.valueOf("缺少角色"));
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

        if(userLoginVO.getRememberMe()){
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
        userPrivacy.setCreatedAt(now);
        userPrivacy.setUpdatedAt(now);
        userPrivacyService.save(userPrivacy);
    }

}




