package com.fzg.vo;


import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


/**
 * 用户注册VO
 */
@Data
public class RegisterVO {


    public interface RegisterVOValidated{

    }

    private String username;

    @Email(groups = {RegisterVOValidated.class},message = "邮箱格式不正确")
    private String email;

    @Size(min = 6,max = 20,groups = {RegisterVOValidated.class},message = "密码长度为6-20位")
    private String password;

    private String code;

}
