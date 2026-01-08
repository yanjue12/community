package com.fzg.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文章标签表
 * article_tag
 */
@Data
public class ArticleTag implements Serializable {
    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签标识
     */
    private String slug;

    /**
     * 描述
     */
    private String description;

    /**
     * 标签颜色
     */
    private String color;

    /**
     * 文章数量
     */
    private Integer articleCount;

    /**
     * 创建时间
     */
    private Date createdAt;

    private static final long serialVersionUID = 1L;
}