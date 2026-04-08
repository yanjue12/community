package com.fzg.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ArticleVO {
    private Long id;
    private String title;
    private String summary;//摘要
    private String coverImage;//封面图片
    private String type;//文章类型
    private Date updatedAt;
    private Date publishedAt;

    private String status;
    private String isTop;
    private String isRecommend;

    private String categoryName;

    private Long userId;
    private String avatar;//用户头像
    private String nickName;

    private String username;
    private String email;
    private String location;
    private String userStatus;

    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer shareCount;

    private String tagName;//多个逗号分割

    private String hotScore;//热度分
    private Boolean liked;
    private Boolean favorited;

}
