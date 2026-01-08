package com.fzg.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@TableName("admin")
@Schema(name = "Admin", description = "管理员表")
public class Admin implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "账号")
    private String account;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;


    @Schema(description = "邮箱")
    private String email;


}

