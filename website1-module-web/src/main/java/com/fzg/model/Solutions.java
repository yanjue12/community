package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;

/**
 * solution表
 * @TableName solutions
 */
@TableName(value ="solutions")
@Data
public class Solutions {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;

    /**
     * 图片url
     */
    @Schema(description = "图片url")
    private String imageUrl;


    @Schema(description = "标题")
    private String title;

    /**
     * 摘要
     */
    @Schema(description = "摘要")
    private String introduction;




    /**
     * 0-下架 1-上架
     */
    @Schema(description = "0-下架 1-上架")
    private Integer states;

    /**
     * 
     */
    private Date createdAt;

    /**
     * 
     */
    private Date updatedAt;


}