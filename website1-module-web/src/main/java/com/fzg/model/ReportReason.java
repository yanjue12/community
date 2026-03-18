package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 举报原因配置表
 * report_reason
 */
@Data
public class ReportReason implements Serializable {
    /**
     * 原因ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 原因代码
     */
    @TableField("code")
    private String code;

    /**
     * 原因名称
     */
    @TableField("name")
    private String name;

    /**
     * 原因描述
     */
    @TableField("description")
    private String description;

    /**
     * 适用的目标类型，多个用逗号分隔
     */
    @TableField("target_types")
    private String targetTypes;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态：active-启用, inactive-禁用
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private Date updatedAt;

    private static final long serialVersionUID = 1L;
}