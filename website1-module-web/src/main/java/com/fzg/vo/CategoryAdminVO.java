package com.fzg.vo;

import lombok.Data;
import java.util.Date;

/**
 * 管理端分类列表 VO
 */
@Data
public class CategoryAdminVO {

    private Long id;
    private String name;
    private String slug;
    private String icon;
    private String description;
    private String level;           // 1/2/3
    private String levelText;       // 一级/二级/三级

    private Long parentId;
    private String parentName;      // 父分类名称（一级分类为空）

    private Integer childCount;     // 直接子分类数量

    private Integer articleCount;   // 当前分类文章总数
    private Integer todayArticleCount;      // 今日（当前时间）文章数
    private Integer yesterdayArticleCount;  // 昨日文章数
    /** 趋势：up / down / stable */
    private String trend;

    private String status;
    private String statusText;      // 启用/禁用
    private String isRecommend;
    private String isNav;
    private Integer sort;
    private Date createdAt;
    private Date updatedAt;
}
