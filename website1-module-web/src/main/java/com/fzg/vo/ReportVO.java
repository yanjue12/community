package com.fzg.vo;

import lombok.Data;
import java.util.Date;

/**
 * 举报信息VO
 */
@Data
public class ReportVO {
    
    /**
     * 举报ID
     */
    private Long id;

    /**
     * 举报人ID
     */
    private Long reporterId;
    private String avatar;

    /**
     * 举报人昵称
     */
    private String reporterName;

    /**
     * 举报目标类型
     */
    private String targetType;

    /**
     * 举报目标ID
     */
    private Long targetId;

    /**
     * 目标标题/内容摘要
     */
    private String targetTitle;

    /**
     * 被举报用户ID
     */
    private Long targetUserId;

    /**
     * 被举报用户昵称
     */
    private String targetUserName;

    /**
     * 举报原因类型
     */
    private String reasonType;

    /**
     * 举报原因名称
     */
    private String reasonName;

    /**
     * 详细举报原因
     */
    private String reasonDetail;

    /**
     * 举报证据图片URLs
     */
    private String evidenceUrls;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 状态描述
     */
    private String statusText;

    /**
     * 处理管理员ID
     */
    private Long adminId;

    /**
     * 处理管理员昵称
     */
    private String adminName;

    /**
     * 管理员处理备注
     */
    private String adminRemark;

    /**
     * 处理时间
     */
    private Date processedAt;

    /**
     * 举报时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 优先级：high-高, medium-中, low-低（根据 reasonType 动态计算）
     */
    private String priority;
}