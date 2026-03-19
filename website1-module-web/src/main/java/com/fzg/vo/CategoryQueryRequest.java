package com.fzg.vo;

import lombok.Data;

/**
 * 管理端分类查询条件
 */
@Data
public class CategoryQueryRequest {

    /** 分类名称（模糊） */
    private String name;

    /** 状态：0-禁用, 1-启用；不传查全部 */
    private String status;

    /** 分类级别：1/2/3；不传查全部 */
    private String level;

    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
