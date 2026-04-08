package com.fzg.controller.app;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fzg.constant.RedisFollowKey;
import com.fzg.constant.RedisVerificationKey;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.Rolemapper;
import com.fzg.mapper.UserRolemapper;
import com.fzg.model.Article;
import com.fzg.model.Follow;
import com.fzg.model.Result;
import com.fzg.model.Role;
import com.fzg.model.User;
import com.fzg.model.UserPrivacy;
import com.fzg.model.UserRole;
import com.fzg.service.UserPrivacyService;
import com.fzg.service.UserService;
import com.fzg.service.FollowService;
import com.fzg.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Schema(name = "用户模块", description = "用户模块")
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserPrivacyService userPrivacyService;
    private final Articlemapper articlemapper;
    private final UserRolemapper userRolemapper;
    private final Rolemapper rolemapper;
    private final FollowService followService;

    @Autowired
    private RedisTemplate redisTemplate;



    @PostMapping("/updatePrivateSetting")
    @Schema(name = "用户模块", description = "用户修改隐私设置")
    public Result updatePrivateSetting(@RequestBody UpdatePrivateSettingVO upPriSetVO){
        if(null == upPriSetVO){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        if(null == upPriSetVO.getUserId()){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        Boolean b = userService.updatePrivateSetting(upPriSetVO);
        return Result.handle(b);
    }

    @PostMapping("/queryPrivateSetting")
    @Schema(name = "用户模块", description = "用户查询隐私设置")
    public Result queryPrivateSetting(@RequestBody UpdatePrivateSettingVO upSetVO){
        if(null == upSetVO || null == upSetVO.getUserId()){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        UserPrivacy userPrivacy;
        Article article;
        if("ALL".equals(upSetVO.getFlag())){
            LambdaQueryWrapper<UserPrivacy> u = new LambdaQueryWrapper<>();
            u.eq(UserPrivacy::getUserId, upSetVO.getUserId());
            userPrivacy = userPrivacyService.getOne(u);
            return Result.success(userPrivacy);
        }else if("MONOMER".equals(upSetVO.getFlag())){
            article = articlemapper.selectById(upSetVO.getArticleId());
            if(null == article){
                return Result.fail(EnumReturn.valueOf("文章不存在"));
            }
            return Result.success(article);
        }
        return Result.fail(EnumReturn.valueOf("入参flag传递错误"));
    }





    /**
     * 发布文章
     * @param articleVO
     * @return
     */
    @PostMapping("/publishArticle")
    public Result publishArticle(@Validated @RequestBody Article articleVO){
        if(null == articleVO){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        if(StringUtils.isEmpty(articleVO.getTitle()) ||
                StringUtils.isEmpty(articleVO.getContent())
                || null == articleVO.getCategoryId()||
                null == articleVO.getUserId()
                || StringUtils.isEmpty(articleVO.getType())){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }

        Boolean b = userService.publishArticle(articleVO);

        return b ? Result.success("发布成功") : Result.fail(EnumReturn.valueOf("发布失败"));

    }


    @PostMapping("/updateArticle")
    public Result updateArticle(@Validated @RequestBody Article articleVO){
        if(null == articleVO){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        if(StringUtils.isEmpty(articleVO.getTitle()) || StringUtils.isEmpty(articleVO.getContent())
                || null == articleVO.getCategoryId()
                || null == articleVO.getUserId()
                || StringUtils.isEmpty(articleVO.getType())){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        if(null == articleVO.getId() || articleVO.getId() <= 0){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }

        Boolean b = userService.updateArticle(articleVO);

        return b ? Result.success("修改成功") : Result.fail(EnumReturn.OPERATION_FAIL);
    }





    @GetMapping("/active")
    public Result queryActiveUser(@RequestParam(required = false,defaultValue = "7") Integer days,
                                  @RequestParam(required = false,defaultValue = "10") Integer size){

        return Result.success(userService.queryActiveUser(days,size));
    }



    @PostMapping("/queryUserInfo")
    public Result queryUserInfo(@RequestBody UserVO userVO) {
        if (userVO == null || userVO.getUserId() == null) {
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        log.info("userId:{}",userVO.getUserId());
        User user = userService.getById(Long.valueOf(userVO.getUserId()));
        if (user == null) {
            return Result.fail(EnumReturn.valueOf("用户不存在"));
        }
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setSignature(user.getSignature());
        userVO.setLocation(user.getLocation());
        userVO.setTopicCount(user.getTopicCount());
        userVO.setCommentCount(user.getCommentCount());
        userVO.setFollowCount(user.getFollowerCount());
        userVO.setFollowingCount(user.getFollowingCount());
        userVO.setCollectionCount(user.getCollectionCount());
        userVO.setCoverImages(user.getCoverImages());

        boolean isFollow = false;
        Long curUserId = userVO.getCurUserId();
        Long targetUserId = Long.valueOf(userVO.getUserId());
        if (curUserId != null) {
            long followCount = followService.count(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getFollowerId, curUserId)
                            .eq(Follow::getFollowingId, targetUserId)
            );
            isFollow = followCount > 0;

            String followingKey = RedisFollowKey.followingSet(curUserId);
            if (isFollow) {
                redisTemplate.opsForSet().add(followingKey, String.valueOf(targetUserId));
            } else {
                redisTemplate.opsForSet().remove(followingKey, String.valueOf(targetUserId));
            }
        }
        userVO.setFollowStatus(isFollow);
        return Result.success(userVO);
    }




    @Operation(summary = "用户注册接口")
    @PostMapping("/register")
    public Result register(@RequestBody RegisterVO registerVO) {

        return userService.register(registerVO);
    }

    @Operation(summary = "用户发送验证码接口（注册）")
    @PostMapping("/send-code")
    public Result sendVerificationCode(@RequestBody RegisterVO registerVO) {

        return userService.sendVerificationCode(registerVO);
    }


    /**
     * 用户名密码登录
     * @return
     */
   // @ApiOperation("用户登录接口")
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginVO userLoginVO) {

        //用户名 / 邮箱 密码登录
        return userService.login(userLoginVO);

    }

    @PostMapping("/checkUsername")
    @Schema(name = "用户模块", description = "检查用户名是否可用")
    public Result checkUsername(@RequestBody RegisterVO request) {
        if(StringUtils.isEmpty(request.getUsername())){
            return Result.fail(EnumReturn.USERNAME_IS_EMPTY);
        }
        return userService.checkUsername(request);
    }


    @PostMapping("/logout")
    public Result logout() {
        try {
            // 获取当前用户ID用于日志
            Object loginId = StpUtil.getLoginIdDefaultNull();
            log.info("用户{}开始登出", loginId);
            

            
            StpUtil.logout();
            
            log.info("用户{}登出完成", loginId);
            return Result.success("退出成功");
        } catch (Exception e) {
            log.error("用户登出失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("登出失败"));
        }
    }

    /**
     * 测试接口：检查用户角色数据
     */
    @PostMapping("/checkUserRoles")
    public Result checkUserRoles() {
        try {
            String loginId = (String) StpUtil.getLoginId();
            Long userId = Long.valueOf(loginId);
            
            // 检查用户角色关联
            List<UserRole> userRoles = userRolemapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
            );
            
            // 检查角色详情
            List<Role> roles = new ArrayList<>();
            if (!CollectionUtils.isEmpty(userRoles)) {
                List<Long> roleIds = userRoles.stream()
                    .map(UserRole::getRoleId)
                    .collect(Collectors.toList());
                roles = rolemapper.selectBatchIds(roleIds);
            }
            
            // 获取Sa-Token中的角色
            List<String> saTokenRoles = StpUtil.getRoleList(userId);
            List<String> saTokenPermissions = StpUtil.getPermissionList(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("userRoles", userRoles);
            result.put("roles", roles);
            result.put("saTokenRoles", saTokenRoles);
            result.put("saTokenPermissions", saTokenPermissions);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail(EnumReturn.valueOf("检查角色失败: " + e.getMessage()));
        }
    }


    /**
     * 修改密码
     * @param updatePasswordVO 包含旧密码和新密码的请求对象
     * @return 操作结果
     */
    @Operation(summary = "修改密码接口")
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody UpdatePasswordVO updatePasswordVO) {
        //判断验证码是否正确
        if(StringUtils.isEmpty(updatePasswordVO.getCode())){
            return Result.fail(EnumReturn.CODE_IS_EMPTY);
        }

        //从redis中获取验证码
        String verificationCode = (String) redisTemplate.opsForValue()
                .get(RedisVerificationKey.getVerificationCodeKey(updatePasswordVO.getEmail()));

        if(verificationCode == null || !verificationCode.equals(updatePasswordVO.getCode())){
            return Result.fail(EnumReturn.VERIFICATION_CODE_ERROR);
        }

        String userId = (String) StpUtil.getLoginId();
        return userService.updatePassword(Long.valueOf(userId),updatePasswordVO);
    }



    /**
     * 忘记密码进行重置
     * @param forgetPasswordVO 包含验证码和新密码的请求对象
     * @return 操作结果
     */
    @Operation(summary = "忘记密码重置接口")
    @PostMapping("/forgetPassword")
    public Result forgetPassword(@RequestBody ForgetPasswordVO forgetPasswordVO) {
        return userService.forgetPassword(forgetPasswordVO);
    }


    @PostMapping("/queryCurLoginUserInfo")
    public Result queryUserInfo() {
        String loginId = (String) StpUtil.getLoginId();
        User user = userService.getById(Long.valueOf(loginId));
        return Result.success(user);
    }

    @Operation(summary = "用户发送验证码接口（非注册）")
    @PostMapping("/UpEmailByCode")
    public Result sendCode(@RequestBody EmailRequest emailRequest) {

        return userService.sendCode(emailRequest);
    }

    /**
     * 验证验证码接口
     * @param verifyCodeVO
     * @return
     */
    @PostMapping("/verifyCode")
    public Result verifyCode(@RequestBody RegisterVO verifyCodeVO) {
        if(StringUtils.isEmpty(verifyCodeVO.getCode())){
            return Result.fail(EnumReturn.CODE_IS_EMPTY);
        }

        //从redis中获取验证码
        String verificationCode = (String) redisTemplate.opsForValue()
                .get(RedisVerificationKey.getVerificationCodeKey(verifyCodeVO.getEmail()));

        if(verificationCode == null || !verificationCode.equals(verifyCodeVO.getCode())){
            return Result.fail(EnumReturn.VERIFICATION_CODE_ERROR);
        }

        return Result.success(true);
    }

    /**
     * 修改邮箱，最后提交接口
     * @param emailRequest
     * @return
     */
    @PostMapping("/UpEmailEnd")
    public Result editEmail(@RequestBody EmailRequest emailRequest) {
        if(null == emailRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        if(StringUtils.isEmpty(emailRequest.getEmail())){
            return Result.fail(EnumReturn.EMAIL_IS_EMPTY);
        }
        if(StringUtils.isEmpty(emailRequest.getCode())){
            return Result.fail(EnumReturn.CODE_IS_EMPTY);
        }
        try {
            //从redis中获取验证码
            String verificationCode = (String) redisTemplate.opsForValue()
                    .get(RedisVerificationKey.getVerificationCodeKey(emailRequest.getEmail()));

            if(verificationCode == null || !verificationCode.equals(emailRequest.getCode())){
                return Result.fail(EnumReturn.VERIFICATION_CODE_ERROR);
            }

            String loginId =(String) StpUtil.getLoginId();
            User user = userService.getById(Long.valueOf(loginId));
            user.setEmail(emailRequest.getEmail());
            user.setUpdatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
            userService.update(user, new QueryWrapper<>());
        }catch (Exception e){
            log.info("修改邮箱异常:{}",e);
            throw new RuntimeException("修改邮箱异常");
        }
        return Result.success(true);
    }


    /**
     * 修改用户个人信息接口
     * @param user
     * @return
     */
    @PostMapping("/editInfo")
    @Schema(name = "用户模块", description = "用户修改个人信息")
    public Result editInfo(@RequestBody User user) {
        String userId = (String) StpUtil.getLoginId();
        boolean result = false;
        try{
            user.setUpdatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
            result = userService.update(user, new QueryWrapper<User>().eq("id", Long.valueOf(userId)));
        }catch (Exception e){
            log.info("更新用户信息异常:{}",e);
        }
        if(result){
            return Result.success(true);
        }else{
            return Result.fail(EnumReturn.UPDATE_USER_INFO_ERROR);
        }
    }


}
