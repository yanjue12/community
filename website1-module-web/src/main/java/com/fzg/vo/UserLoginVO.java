package com.fzg.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * 用户登录VO
 */
@Data
public class UserLoginVO {
    public interface UserLoginVOValidated{

    }

    private String username;

    private String account;

    private String email;

    @NotBlank(groups = {UserLoginVO.UserLoginVOValidated.class},message = "密码不能为空")
    @Size(min = 6,max = 20,groups = {UserLoginVO.UserLoginVOValidated.class},message = "密码长度为6-20位")
    private String password;

    //状态 0-正常 1-禁用
    @NotNull(message = "状态不能为空 1-正常 2-禁用")
    private  Short states;


}
