package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 私信会话表
 * private_conversation
 */
@Data
@TableName("private_conversation")
public class PrivateConversation implements Serializable {
    /**
     * 会话ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户1 ID
     */
    private Long user1Id;

    /**
     * 用户2 ID
     */
    private Long user2Id;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 最后消息内容(预览)
     */
    private String lastMessageContent;

    /**
     * 最后消息时间
     */
    private Date lastMessageTime;

    /**
     * 用户1未读消息数
     */
    private Integer unreadCount1;

    /**
     * 用户2未读消息数
     */
    private Integer unreadCount2;

    /**
     * 用户1是否删除会话
     */
    private String isDeleted1;

    /**
     * 用户2是否删除会话
     */
    private String isDeleted2;

    /**
     * 状态 0:关闭 1:正常
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    private static final long serialVersionUID = 1L;
}