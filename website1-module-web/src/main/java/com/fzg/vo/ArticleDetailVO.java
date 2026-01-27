package com.fzg.vo;

import com.fzg.model.Comment;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ArticleDetailVO{
    private Long articleId;
    private String title;
    private String content;
    private String tag;
    private String summary;
    private String contentHtml;
    private String createTime;
    private String updateTime;
    private String isOriginal;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer viewCount;
    private Integer shareCount;
    private Integer wordCount;
    private Date publishAt;

    private Long userId;
    private String nickName;
    private String avatar;
    private String introduction;

    private List<CommentVO> commnet;

}
