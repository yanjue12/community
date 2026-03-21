package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 配置分组表
 * system_config_group
 */
@Data
@TableName("system_config_group")
public class SystemConfigGroup implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 分组标识 */
    private String groupName;

    /** 分组名称（显示用） */
    private String groupLabel;

    /** 排序 */
    private Integer sort;

    /** 状态 0禁用 1启用 */
    private String status;

    private static final long serialVersionUID = 1L;
}
