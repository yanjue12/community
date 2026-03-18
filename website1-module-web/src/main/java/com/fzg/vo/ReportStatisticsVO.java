package com.fzg.vo;

import lombok.Data;

/**
 * 举报统计VO
 */
@Data
public class ReportStatisticsVO {
    
    /**
     * 待处理数量
     */
    private Long pendingCount;

    /**
     * 今日举报数量
     */
    private Long todayCount;

    /**
     * 本周已处理数量
     */
    private Long weekResolvedCount;

    /**
     * 处理率
     */
    private String processRate;

    /**
     * 总举报数量
     */
    private Long totalCount;

    /**
     * 已处理数量
     */
    private Long resolvedCount;

    /**
     * 已拒绝数量
     */
    private Long rejectedCount;

    /**
     * 处理中数量
     */
    private Long processingCount;
}