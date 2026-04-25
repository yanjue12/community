package com.fzg.vo;

import lombok.Data;

@Data
public class BotMultimodalEmbeddingRequest {
    private Long userId;
    private String model;
    private String text;
    private String imageUrl;
    private String prompt;
}
