package com.fzg.vo;

import lombok.Data;


/**
 * 用户登录VO
 */
@Data
public class UserLoginVO {

    private String username;

    private String account;

    private String email;

    private String password;

    //状态 0-正常 1-禁用
    private  Short status;


}
