package com.fzg.vo;

import lombok.Data;

/**
 * 举报请求VO
 */
@Data
public class ReportRequest {
    
    /**
     * 举报人ID
     */
    private Long reporterId;

    /**
     * 举报目标类型：article-文章, comment-评论, user-用户
     */
    private String targetType;

    /**
     * 举报目标ID
     */
    private Long targetId;

    /**
     * 被举报用户ID
     */
    private Long targetUserId;

    /**
     * 举报原因类型
     */
    private String reasonType;

    /**
     * 详细举报原因
     */
    private String reasonDetail;

    /**
     * 举报证据图片URLs，多个用逗号分隔
     */
    private String evidenceUrls;
}