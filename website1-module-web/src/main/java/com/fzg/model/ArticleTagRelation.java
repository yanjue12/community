package com.fzg.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 文章标签关联表
 * article_tag_relation
 */
@Data
public class ArticleTagRelation implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 创建时间
     */
    private Date createdAt;

    private static final long serialVersionUID = 1L;
}