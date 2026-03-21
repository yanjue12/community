package com.fzg.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ConversationVO {

    private Long conversationId;

    private Long targetUserId;
    private String nickname;
    private String avatar;

    private String lastMessage;
    private Date lastMessageTime;

    private Integer unreadCount;
}