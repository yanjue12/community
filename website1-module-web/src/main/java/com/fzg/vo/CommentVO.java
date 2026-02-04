package com.fzg.vo;

import lombok.Data;

import java.util.Date;

@Data
public class CommentVO {
        /**
         * 评论ID
         */
        private Long id;
        private Long userId;
        private Long authorId;//作者id
        private Long articleId;
        private Long parentId;
        private Long replyToUserId;
        private Long replyToCommentId;
        private String content;
        private String contentHtml;

        private Integer likeCount;

        private Integer dislikeCount;

        /**
         * 状态 0:删除 1:正常 2:审核
         */
        private String status;

        /**
         * 是否作者回复
         */
        private String isAuthor;

        private String ip;

        /**
         * 用户代理
         */
        private String userAgent;

        private Date createdAt;
        private Date updatedAt;

        private String nickname;
        private String avatar;
        private Long rootId;
        private Integer replyCount;
}
