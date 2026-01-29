package com.fzg.vo;

import lombok.Data;

@Data
public class ReadTimeRequest {
    private Long articleId;
    //当前登录人id
    private Long userId;
    private Long authorId;//作者id
    private Integer duration;
    private String ip;
}
