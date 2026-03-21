package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 私信消息表
 * private_message
 */
@Data
@TableName("private_message")
public class PrivateMessage implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    private Long senderId;

    private Long receiverId;

    private String content;

    /** 消息类型 1:文本 2:图片 3:文件 4:链接 */
    private String contentType;

    /** 是否已读 0:未读 1:已读 */
    private Integer isRead;

    private Date createdAt;

    /** 是否删除 0:否 1:是 */
    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}
