package com.fzg.vo;

import lombok.Data;
import java.util.Date;

/**
 * 审核记录VO
 */
@Data
public class AuditRecordVO {
    
    /**
     * 审核记录ID
     */
    private Long id;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 文章封面图
     */
    private String coverImage;

    /**
     * 文章字数
     */
    private Integer wordCount;

    /**
     * 文章标签
     */
    private String tags;
    private String categoryName;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer collectCount;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 作者昵称
     */
    private String authorNickname;

    /**
     * 作者头像
     */
    private String authorAvatar;

    /**
     * 文章创建时间
     */
    private Date articleCreatedAt;

    /**
     * 审核状态：0-待审核, 1-通过, 2-拒绝
     */
    private Byte auditStatus;

    /**
     * 审核状态文本
     */
    private String auditStatusText;

    /**
     * 审核类型：1-自动, 2-人工
     */
    private Byte auditType;

    /**
     * 审核类型文本
     */
    private String auditTypeText;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核人昵称
     */
    private String auditorNickname;

    /**
     * 审核原因/备注
     */
    private String reason;

    /**
     * 审核记录创建时间
     */
    private Date createdAt;

    /**
     * 审核记录更新时间
     */
    private Date updatedAt;

    /**
     * 时间描述（如：2小时前）
     */
    private String timeDescription;
}