package com.fzg.vo;

import lombok.Data;

@Data
public class ArticleRequest {
    private Long userId;
    //获取帖子类型 0：热榜 1：推荐 2：关注 3：最新
    private String type;

    private Integer pageNum;

    private Integer pageSize;
}
