package com.fzg.vo;

import lombok.Data;

/**
 * 用户管理卡片统计数据
 */
@Data
public class UserStatsVO {
    /** 总用户数 */
    private Long totalCount;
    /** 正常用户数 */
    private Long normalCount;
    /** 已封禁用户数 */
    private Long bannedCount;
    /** 管理员数量 */
    private Long adminCount;
}
