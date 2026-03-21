package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 私信会话表
 * private_conversation
 */
@Data
@TableName("private_conversation")
public class PrivateConversation implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 两个用户ID中较小的那个 */
    private Long userMin;

    /** 两个用户ID中较大的那个 */
    private Long userMax;

    private Long lastMessageId;

    private Date lastMessageTime;

    private Date createdAt;

    private Date updatedAt;

    private static final long serialVersionUID = 1L;
}
