package com.fzg.vo;

import lombok.Data;

/**
 * 分类统计VO
 */
@Data
public class CategoryStatisticsVO {
    
    /**
     * 总分类数
     */
    private Long totalCategories;

    /**
     * 启用中分类数
     */
    private Long enabledCategories;

    /**
     * 总文章数
     */
    private Long totalArticles;

    /**
     * 已归档分类数（禁用的分类）
     */
    private Long archivedCategories;

    /**
     * 一级分类数量
     */
    private Long level1Count;

    /**
     * 二级分类数量
     */
    private Long level2Count;

    /**
     * 三级分类数量
     */
    private Long level3Count;

    /**
     * 推荐分类数量
     */
    private Long recommendCategories;

    /**
     * 导航显示分类数量
     */
    private Long navCategories;

    /**
     * 启用率
     */
    private String enabledRate;

    /**
     * 平均每个分类的文章数
     */
    private Double avgArticlesPerCategory;
}