package com.fzg.model;

import java.io.Serializable;
import java.util.Date;
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
     * 类型 1:系统 2:评论 3:回复 4:点赞 5:关注 6:私信 7:收藏 8:提到
     */
    private String type;

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

    private static final long serialVersionUID = 1L;
}