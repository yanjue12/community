package com.fzg.vo;

import lombok.Data;

@Data
public class ArticleRequest {
    private Long articleId;
    //当前登录人的id
    private Long userId;
    private Long draftId;
    private Long authorId;
    //获取帖子类型 0：热榜 1：推荐 2：关注 3：最新
    private String type;
    private String ip;

    private String title;

    private String content;

    private String tag;

    private String summary;//摘要

    private String contentHtml;

    private String categoryName;

    private String coverImage;

    private Integer pageNum;

    private Integer pageSize;

    // 新增的请求参数
    private String status;
    private String isTop;
    private String isRecommend;
    private String startTime;
    private String endTime;
    private Integer minViews;
    private Integer maxViews;
    private Integer minLikes;
    private Integer maxLikes;
}
