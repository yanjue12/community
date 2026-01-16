package com.fzg.controller.app;


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fzg.constant.RedisVerificationKey;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.model.User;
import com.fzg.service.UserService;
import com.fzg.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Schema(name = "用户模块", description = "用户模块")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/active")
    public Result queryActiveUser(@RequestParam(required = false,defaultValue = "7") Integer days,
                                  @RequestParam(required = false,defaultValue = "10") Integer size){

        return Result.success(userService.queryActiveUser(days,size));
    }



    @PostMapping("/queryUserInfo")
    public Result queryUserInfo(@RequestBody UserVO userVO) {
        User user = userService.getById(Long.valueOf(userVO.getUserId()));
        return Result.success(user);
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

        StpUtil.logout();
        return Result.success("退出成功");

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
