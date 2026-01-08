package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 新闻类型表
 */
@TableName(value ="news_type")
@Data
public class NewsType {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Short id;

    /**
     * 新闻类型：如：Product ，Event，Partnership
     */
    @Schema(description = "新闻类型名：如：Product ，Event，Partnership")
    private String name;

    /**
     * 
     */
    private LocalDateTime createdAt;

    /**
     * 
     */
    private LocalDateTime updatedAt;

}