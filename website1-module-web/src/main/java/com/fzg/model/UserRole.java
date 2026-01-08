package com.fzg.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户角色关联表
 * user_role
 */
@Data
public class UserRole implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 创建时间
     */
    private Date createdAt;

    private static final long serialVersionUID = 1L;
}