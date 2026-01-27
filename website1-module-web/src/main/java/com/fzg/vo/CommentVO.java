package com.fzg.vo;

import lombok.Data;

import java.util.Date;

@Data
public class CommentVO {
    private Long id;
    private Long articleId;
    private Long userId;
    private Long parentId;
    private Long replyToUserId;
    private Long replyToCommentId;
    private String content;
    private Integer likeCount;
    private String isAuthor;
    private String ip;
    private Date createdAt;
}
