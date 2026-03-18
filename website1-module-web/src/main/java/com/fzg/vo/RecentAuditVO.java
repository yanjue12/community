package com.fzg.vo;

import lombok.Data;
import java.util.Date;

/**
 * 最近审核记录VO
 */
@Data
public class RecentAuditVO {
    
    /**
     * 审核记录ID
     */
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 作者昵称
     */
    private String authorNickname;

    /**
     * 审核人昵称
     */
    private String auditorNickname;

    /**
     * 审核状态：1-已通过, 2-已拒绝
     */
    private Byte auditStatus;

    /**
     * 审核状态文本
     */
    private String auditStatusText;

    /**
     * 审核原因/备注
     */
    private String reason;

    /**
     * 审核时间
     */
    private Date updatedAt;

    /**
     * 审核时间描述（如：2小时前）
     */
    private String timeDescription;
}