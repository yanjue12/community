package com.fzg.vo;

import lombok.Data;

/**
 * 举报列表查询请求
 */
@Data
public class ReportQueryRequest {

    /** 关键词（举报内容/被举报用户名） */
    private String keyword;

    /** 目标类型：article, comment, user */
    private String targetType;

    /** 举报原因类型：spam, inappropriate, harassment, copyright, other */
    private String reasonType;

    /** 优先级：high, medium, low（前端筛选，后端转换为 reasonType 范围） */
    private String priority;

    /** 处理状态：pending, processing, resolved, rejected；不传默认 pending */
    private String status;

    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
