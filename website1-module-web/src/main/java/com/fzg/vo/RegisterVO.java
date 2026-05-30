package com.fzg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.EAN;

import javax.validation.constraints.Email;

@Schema(name = "用户注册参数")
@Data
public class RegisterVO {

    private String username;
    private String password;
    private String phoneNumber;
    @Email
    private String email;
    private String code;
    private String outId;
    private String bizId;
    private String captchaTicket;
    private String captchaRandStr;
    private String captchaId;
    private String captchaCode;


}
