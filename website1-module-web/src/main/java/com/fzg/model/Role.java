package com.fzg.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 角色表
 * role
 */
@Data
public class Role implements Serializable {
    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态 0:禁用 1:启用
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createdAt;

    private static final long serialVersionUID = 1L;
}