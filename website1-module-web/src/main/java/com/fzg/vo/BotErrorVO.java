package com.fzg.vo;

import lombok.Data;

@Data
public class BotErrorVO {
    private String requestId;
    private Long timestamp;
    private String errorCode;
    private String errorMessage;
}
