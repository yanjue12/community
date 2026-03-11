package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 通知表
 * notification
 */
@Data
public class Notification implements Serializable {
    /**
     * 通知ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 发送用户ID
     */
    private Long fromUserId;

    /**
     * 类型 system:系统消息 user:用户互动 message:私信提醒
     */
    private String type;

    /**
     * 动作类型（用于细分，如like_article, like_comment, follow, reply等）
     */
    private String actionType;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 目标类型
     */
    private String targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 父级ID（如回复的评论ID）
     */
    private Long parentId;

    /**
     * 额外数据
     */
    private Object extraData;

    /**
     * 是否已读
     */
    private String isRead;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 阅读时间
     */
    private Date readAt;

    /**
     * 来源对象ID(如评论ID/文章ID)
     */
    private Long sourceId;

    /**
     * 来源对象类型(comment/article/message)
     */
    private String sourceType;

    /**
     * 通知聚合ID
     */
    private String groupId;

    /**
     * 是否删除
     */
    private String isDeleted;

    /**
     * 通知级别(normal/important)
     */
    private String notifyLevel;

    private static final long serialVersionUID = 1L;
}