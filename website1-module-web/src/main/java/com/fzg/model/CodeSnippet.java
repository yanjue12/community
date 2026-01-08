package com.fzg.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 代码片段表
 * code_snippet
 */
@Data
public class CodeSnippet implements Serializable {
    /**
     * 代码ID
     */
    private Long id;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 代码内容
     */
    private String code;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 标签
     */
    private String tags;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 可见性 0:公开 1:私密 2:仅关注
     */
    private String visibility;

    /**
     * 是否fork
     */
    private String isFork;

    /**
     * 来源代码ID
     */
    private Long forkFromId;

    /**
     * 查看次数
     */
    private Integer viewCount;

    /**
     * 运行次数
     */
    private Integer runCount;

    /**
     * fork次数
     */
    private Integer forkCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 代码大小(字节)
     */
    private Integer codeSize;

    /**
     * 行数
     */
    private Integer lineCount;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    private static final long serialVersionUID = 1L;
}