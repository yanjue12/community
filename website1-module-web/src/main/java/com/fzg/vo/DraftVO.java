package com.fzg.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class DraftVO {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String summary;

    /**
     * 内容（HTML格式）
     */
    private String content;

    /**
     * 原始内容（Markdown等）
     */
    private String contentRaw;

    /**
     * 内容类型:html/markdown
     */
    private String contentType;
    private String coverImage;

    /**
     * 模块类型:1文章/2问答/3笔记/4项目
     */
    private String moduleType;

    /**
     * 关联正式内容ID（编辑时）
     */
    private Long moduleId;

    private Long categoryId;
    private String categoryName;

    /**
     * 标签，多个用逗号分隔
     */
    private String tags;

    /**
     * 可见性:0公开 1私密 2仅粉丝 3密码访问
     */
    private String visibility;

    /**
     * 访问密码（当visibility=3时）
     */
    private String password;

    /**
     * 格式:1富文本 2Markdown
     */
    private Byte format;

    /**
     * 语言
     */
    private String language;

    /**
     * 版权类型:原创/转载/翻译
     */
    private String copyright;

    private String originalUrl;

    /**
     * 问题类型:技术/求职/生活等
     */
    private String questionType;


    private Integer rewardPoints;

    private Long bestAnswerId;

    /**
     * 项目类型:开源/商业/个人
     */
    private String projectType;

    private String techStack;
    private String githubUrl;

    /**
     * 演示地址
     */
    private String demoUrl;

    /**
     * 项目状态:ongoing/completed/archived
     */
    private String projectStatus;
    private Integer wordCount;
    private Integer readingTime;
    private Integer charCount;
    private Integer imageCount;
    private Integer codeBlockCount;
    private String saveReason;
    private Integer version;
    private Long parentDraftId;
    private Date lastModifiedAt;
    private Date createdAt;

    private String extraData;

}
