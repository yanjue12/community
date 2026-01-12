package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 分类表
 * category
 */
@Data
public class Category implements Serializable {
    /**
     * 分类ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父分类ID，0表示顶级分类
     */
    private Long parentId;

    /**
     * 分类层级 1:一级 2:二级 3:三级
     */
    private String level;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类标识（用于URL）
     */
    private String slug;

    /**
     * 图标
     */
    private String icon;

    /**
     * 描述
     */
    private String description;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 文章数量
     */
    private Integer articleCount;

    /**
     * 是否推荐
     */
    private String isRecommend;

    /**
     * 是否在导航显示
     */
    private String isNav;

    /**
     * 状态 0:禁用 1:启用
     */
    private String status;

    /**
     * SEO标题
     */
    private String seoTitle;

    /**
     * SEO关键词
     */
    private String seoKeywords;

    /**
     * SEO描述
     */
    private String seoDescription;

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