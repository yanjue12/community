package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户第三方授权表
 * user_oauth
 */
@Data
@TableName("user_oauth")
public class UserOauth implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 授权类型 如 github/google/wechat */
    private String oauthType;

    /** 第三方平台用户唯一标识 */
    private String openId;

    private String accessToken;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
