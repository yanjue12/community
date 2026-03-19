package com.fzg.vo;

import lombok.Data;

/**
 * 文章统计数据 VO
 */
@Data
public class ArticleStatsVO {

    /** 文章总数（排除已删除） */
    private Long total;

    /** 已发布数量（status=1） */
    private Long published;

    /** 待审核数量（status=2） */
    private Long pending;

    /** 本周新增数量 */
    private Long weeklyNew;
}
