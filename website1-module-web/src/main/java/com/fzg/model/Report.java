package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 举报表
 * report
 */
@Data
public class Report implements Serializable {
    /**
     * 举报ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 举报人ID
     */
    @TableField("reporter_id")
    private Long reporterId;

    /**
     * 举报目标类型：article-文章, comment-评论, user-用户
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 举报目标ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 被举报用户ID
     */
    @TableField("target_user_id")
    private Long targetUserId;

    /**
     * 举报原因类型：spam-垃圾信息, inappropriate-不当内容, harassment-骚扰, copyright-版权, other-其他
     */
    @TableField("reason_type")
    private String reasonType;

    /**
     * 详细举报原因
     */
    @TableField("reason_detail")
    private String reasonDetail;

    /**
     * 举报证据图片URLs，多个用逗号分隔
     */
    @TableField("evidence_urls")
    private String evidenceUrls;

    /**
     * 处理状态：pending-待处理, processing-处理中, resolved-已处理, rejected-已驳回
     */
    @TableField("status")
    private String status;

    /**
     * 处理管理员ID
     */
    @TableField("admin_id")
    private Long adminId;

    /**
     * 管理员处理备注
     */
    @TableField("admin_remark")
    private String adminRemark;

    /**
     * 处理时间
     */
    @TableField("processed_at")
    private Date processedAt;

    /**
     * 举报时间
     */
    @TableField("created_at")
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private Date updatedAt;

    private static final long serialVersionUID = 1L;
}