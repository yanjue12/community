package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "news_detail")

public class NewsDetail {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "新闻id")
    private Integer newsId;


    @Schema(description = "新闻内容")
    private String content;


    @Schema(description = "创建时间")
    private LocalDateTime createdAt;


    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;


}
