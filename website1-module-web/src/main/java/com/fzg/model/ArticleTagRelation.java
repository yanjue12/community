package com.fzg.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
    @TableId(value = "id", type = IdType.AUTO)
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

    private Integer hitCount;//命中次数
    private BigDecimal density;//密度
    private BigDecimal score;//成立评分
    private String source;//1系统生成 2用户补充
    private String confirmed;//是否人工确认

    private static final long serialVersionUID = 1L;
}