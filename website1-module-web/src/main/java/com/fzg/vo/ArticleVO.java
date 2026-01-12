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
    private Date updatedTime;

    private String categoryName;

    private Long userId;
    private String avatar;//用户头像
    private String nickName;

    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer collectCount;
    private Integer shareCount;

    private String tagName;//多个逗号分割

    private String hotScore;//热度分

}
