package com.fzg.model; // 修改包名为正确路径

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 新闻表
 * @TableName news
 */
@TableName(value ="news")
@Data
public class News {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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

    @Schema(description = "新闻url")
    private String url;


    /**
     * 发布时间
     */
    @Schema(description = "发布时间")
    private Date publishDate;

    /**
     * 发布人
     */
    @Schema(description = "发布人")
    private String author;



    /**
     * 
     */
    @Schema(description = "创建时间")
    private Date createdAt;

    /**
     * 
     */
    @Schema(description = "更新时间")
    private Date updatedAt;


}