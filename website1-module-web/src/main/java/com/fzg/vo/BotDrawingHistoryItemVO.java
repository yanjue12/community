package com.fzg.vo;

import lombok.Data;

@Data
public class BotDrawingHistoryItemVO {
    private String recordId;
    private Long userId;
    private String model;
    private String prompt;
    private String text;
    private String imageUrl;
    private Long timestamp;
}
