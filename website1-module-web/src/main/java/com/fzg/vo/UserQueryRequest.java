package com.fzg.vo;

import lombok.Data;

/**
 * 用户列表查询条件
 */
@Data
public class UserQueryRequest {
    /** 用户名/昵称/邮箱模糊搜索 */
    private String keyword;
    /** 角色ID */
    private Long roleId;
    /** 状态 0:正常 1:禁用 */
    private String status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
