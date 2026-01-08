package com.fzg.controller.app;


import cn.dev33.satoken.stp.StpUtil;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.service.UserService;
import com.fzg.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Schema(name = "用户模块", description = "用户模块")
@Slf4j
public class UserController {

    private final UserService userService;


    @Operation(summary = "用户注册接口")
    @PostMapping("/register")
    public Result register(@RequestBody RegisterVO registerVO) {

        return userService.register(registerVO);
    }

    @Operation(summary = "用户发送验证码接口")
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
     * 修改用户名
     * @param updateUsernameVO 包含新用户名的请求对象
     * @return 操作结果
     */
    @Operation(summary = "修改用户名接口")
    @PostMapping("/updateUsername")
    public Result updateUsername(@RequestBody UpdateUsernameVO updateUsernameVO) {
        Integer userId = (Integer) StpUtil.getLoginId();
        return userService.updateUsername(userId,updateUsernameVO);
    }



    /**
     * 修改密码
     * @param updatePasswordVO 包含旧密码和新密码的请求对象
     * @return 操作结果
     */
    @Operation(summary = "修改密码接口")
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody UpdatePasswordVO updatePasswordVO) {
        Integer userId = (Integer) StpUtil.getLoginId();
        return userService.updatePassword(userId,updatePasswordVO);
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





}
