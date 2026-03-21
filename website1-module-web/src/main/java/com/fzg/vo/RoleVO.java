package com.fzg.vo;

import com.fzg.model.Permission;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 角色详情 VO（含权限列表和用户数量）
 */
@Data
public class RoleVO {

    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer sort;
    private String status;
    private Date createdAt;
    private Date updatedAt;

    /** 该角色下的用户数量 */
    private Long userCount;

    /** 该角色拥有的权限列表 */
    private List<Permission> permissions;
}
