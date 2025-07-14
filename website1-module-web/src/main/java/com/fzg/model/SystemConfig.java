package com.fzg.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value ="system_config")
public class SystemConfig {

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private Date createdAt;


    @Schema(description = "更新时间")
    private Date updatedAt;
}
