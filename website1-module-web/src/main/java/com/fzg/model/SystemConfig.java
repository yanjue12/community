package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统配置表
 * system_config
 */
@Data
@TableName("system_config")
public class SystemConfig implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 配置键（唯一标识） */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 类型 string/number/boolean/json */
    private String valueType;

    /** 分组 system/feature/security/email/upload */
    private String groupName;

    /** 配置名称（用于前端显示） */
    private String configName;

    /** 描述 */
    private String description;

    /** 是否前端可见 0否 1是 */
    private Integer isPublic;

    /** 排序 */
    private Integer sort;

    /** 状态 0禁用 1启用 */
    private String status;

    private Date createdAt;

    private Date updatedAt;

    private static final long serialVersionUID = 1L;
}
