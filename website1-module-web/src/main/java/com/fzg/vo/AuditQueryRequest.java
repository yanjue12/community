package com.fzg.vo;

import lombok.Data;

/**
 * 审核查询请求VO
 */
@Data
public class AuditQueryRequest {
    
    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 页面大小
     */
    private Integer pageSize;

    /**
     * 审核状态：0-待审核, 1-已通过, 2-已拒绝
     */
    private Byte auditStatus;

    /**
     * 文章标题（模糊查询）
     */
    private String title;

    /**
     * 作者名称（模糊查询）
     */
    private String authorName;

    /**
     * 文章分类ID
     */
    private Long categoryId;

    /**
     * 审核类型：1-自动, 2-人工
     */
    private Byte auditType;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 开始时间（文章创建时间）
     */
    private String startTime;

    /**
     * 结束时间（文章创建时间）
     */
    private String endTime;

    /**
     * 审核开始时间
     */
    private String auditStartTime;

    /**
     * 审核结束时间
     */
    private String auditEndTime;
}