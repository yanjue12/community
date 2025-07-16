package com.fzg.model; // 修改包名为正确路径

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 新闻表
 */
@TableName(value ="news")
@Data
public class News {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 新闻标题
     */
    @Schema(description = "新闻标题")
    private String title;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String summary;

    @Schema(description = "新闻类型")
    private String label;

    @Schema(description = "新闻状态 0:下架 1:已发布")
    private String states;


    @Schema(description = "新闻url")
    private String url;


    /**
     * 发布时间
     */
    @Schema(description = "发布时间")
    private LocalDateTime publishDate;

    /**
     * 发布人
     */
    @Schema(description = "发布人")
    private String author;



    /**
     * 
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;


}