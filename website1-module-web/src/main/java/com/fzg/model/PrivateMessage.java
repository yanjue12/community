package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 私信消息表
 * private_message
 */
@Data
@TableName("private_message")
public class PrivateMessage implements Serializable {
    /**
     * 消息ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型 1:文本 2:图片 3:文件 4:链接
     */
    private String contentType;

    /**
     * 文件URL(图片/文件)
     */
    private String fileUrl;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小(字节)
     */
    private Integer fileSize;

    /**
     * 是否已读 0:未读 1:已读
     */
    private String isRead;

    /**
     * 阅读时间
     */
    private Date readAt;

    /**
     * 发送者是否删除
     */
    private String isDeletedSender;

    /**
     * 接收者是否删除
     */
    private String isDeletedReceiver;

    /**
     * 发送时间
     */
    private Date createdAt;

    private static final long serialVersionUID = 1L;
}