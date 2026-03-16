package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 通知表
 * notification
 */
@Data
public class Notification implements Serializable {
    /**
     * 通知ID (使用雪花算法)
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接收用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 发送用户ID
     */
    @TableField("from_user_id")
    private Long fromUserId;

    /**
     * 类型 system:系统消息 user:用户互动 message:私信提醒
     */
    @TableField("type")
    private String type;

    /**
     * 动作类型（用于细分，如like_article, like_comment, follow, reply等）
     */
    @TableField("action_type")
    private String actionType;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 目标类型
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标ID
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 父级ID（如回复的评论ID）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 额外数据
     */
    @TableField("extra_data")
    private String extraData;

    /**
     * 是否已读
     */
    @TableField("is_read")
    private String isRead;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Date createdAt;

    /**
     * 阅读时间
     */
    @TableField("read_at")
    private Date readAt;

    /**
     * 来源对象ID(如评论ID/文章ID)
     */
    @TableField("source_id")
    private Long sourceId;

    /**
     * 来源对象类型(comment/article/message)
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 通知聚合ID
     */
    @TableField("group_id")
    private String groupId;

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    private String isDeleted;

    /**
     * 通知级别(normal/important)
     */
    @TableField("notify_level")
    private String notifyLevel;

    private static final long serialVersionUID = 1L;
}