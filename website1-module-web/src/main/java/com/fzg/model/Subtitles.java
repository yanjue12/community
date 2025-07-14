package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "subtitles")
public class Subtitles {

    @Schema(description = "主键id")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "解决方案id")
    private Integer solutionId;

    @Schema(description = "子标题")
    private String subtitle;

    @Schema(description = "子标题内容")
    private String description;

    @Schema(description = "状态 0-下架 1-上架")
    private Short states;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private Date createdAt;


    @Schema(description = "更新时间")
    private Date updatedAt;




}
