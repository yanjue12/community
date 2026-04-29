package com.fzg.vo;

import lombok.Data;

@Data
public class BotChatResponseVO {
    private String requestId;
    private String conversationId;
    private Long timestamp;
    private String model;
    private String depth;
    private String answer;
}
