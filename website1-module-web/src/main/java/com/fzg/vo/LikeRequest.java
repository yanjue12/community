package com.fzg.vo;

import lombok.Data;

@Data
public class LikeRequest {
    private Long articleId;
    private Long userId;
    private Integer actionLike; // 0:取消点赞 1:点赞
    private String type;
}
