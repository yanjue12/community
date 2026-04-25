package com.fzg.vo;

import lombok.Data;

@Data
public class BotConversationSessionVO {
    private Long userId;
    private String conversationId;
    private String title;
    private String lastMessage;
    private String model;
    private Long lastTimestamp;
}
