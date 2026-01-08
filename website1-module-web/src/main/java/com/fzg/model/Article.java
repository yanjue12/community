package com.fzg.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文章表
 * article
 */
@Data
public class Article implements Serializable {
    /**
     * 文章ID
     */
    private Long id;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 内容
     */
    private String content;

    /**
     * HTML内容
     */
    private String contentHtml;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 类型 1:文章 2:问答 3:分享
     */
    private String type;

    /**
     * 格式 1:Markdown 2:富文本
     */
    private String format;

    /**
     * 状态 0:草稿 1:已发布 2:审核中 3:审核失败 4:删除
     */
    private String status;

    /**
     * 可见性 0:公开 1:私密 2:仅粉丝
     */
    private String visibility;

    /**
     * 是否置顶
     */
    private String isTop;

    /**
     * 是否推荐
     */
    private String isRecommend;

    /**
     * 是否原创
     */
    private String isOriginal;

    /**
     * 是否可评论
     */
    private String isCommentable;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 分享数
     */
    private Integer shareCount;

    /**
     * 字数
     */
    private Integer wordCount;

    /**
     * 阅读时长(分钟)
     */
    private Integer readingTime;

    /**
     * 最后评论时间
     */
    private Date lastCommentTime;

    /**
     * 发布时间
     */
    private Date publishedAt;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    private static final long serialVersionUID = 1L;
}