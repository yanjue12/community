package com.fzg.vo;

import lombok.Data;

/**
 * 管理端编辑用户请求（状态 + 角色）
 */
@Data
public class UserEditRequest {
    /** 状态 0:正常 1:禁用 */
    private String status;
    /** 角色ID，传 null 表示不修改角色 */
    private Long roleId;
}
