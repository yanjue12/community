package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 文章浏览记录表
 * article_view_history
 */
@Data
@TableName("article_view_history")
public class ArticleViewHistory implements Serializable {
    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 浏览用户ID（匿名用户为空）
     */
    private Long userId;

    /**
     * 浏览者IP
     */
    private String viewerIp;

    /**
     * 浏览者User-Agent
     */
    private String viewerUserAgent;

    /**
     * 浏览时长(秒)
     */
    private Integer viewDuration;

    /**
     * 浏览时间
     */
    private Date createdAt;

    private static final long serialVersionUID = 1L;
}