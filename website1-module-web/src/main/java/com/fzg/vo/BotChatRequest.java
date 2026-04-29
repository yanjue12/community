package com.fzg.vo;

import lombok.Data;

@Data
public class BotChatRequest {
    private Long userId;
    private String conversationId;
    private String model;
    private String message;
    private String depth;
}
