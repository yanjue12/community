package com.fzg.vo;

import lombok.Data;

import java.util.Date;

/**
 * 管理端用户列表 VO
 */
@Data
public class UserAdminVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String status;
    /** 角色ID */
    private Long roleId;
    /** 角色名称 */
    private String roleName;
    /** 文章数 */
    private Integer articleCount;
    private Date createdAt;
}
