package com.fzg.controller.app;


import cn.dev33.satoken.stp.StpUtil;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.service.UserService;
import com.fzg.vo.RegisterVO;
import com.fzg.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/user")
@RequiredArgsConstructor
@Schema(name = "用户模块", description = "用户模块")
@Slf4j
public class UserController {

    private final UserService userService;


    @Operation(summary = "用户注册接口")
    @PostMapping("/register")
    public Result register(@Validated (RegisterVO.RegisterVOValidated.class) @RequestBody RegisterVO registerVO) {

        return userService.register(registerVO);
    }

    @Operation(summary = "用户发送验证码接口")
    @PostMapping("/sendVerificationCode")
    public Result sendVerificationCode(@Validated (RegisterVO.RegisterVOValidated.class) @RequestBody RegisterVO registerVO) {

        return userService.sendVerificationCode(registerVO);
    }



    /**
     * 用户名密码登录
     * @return
     */
   // @ApiOperation("用户登录接口")
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginVO userLoginVO) {


        return userService.accountLogin(userLoginVO);

    }


    @PostMapping("/logout")
    public Result logout() {

        StpUtil.logout();
        return Result.success("退出成功");

    }

    //修改用户名


    //修改密码



    //忘记密码进行重置
    /*@PostMapping("/forgetPassword")
    public Result forgetPassword(@RequestBody RegisterVO registerVO) {

        return userService.forgetPassword(userLoginVO);
    }*/





}
