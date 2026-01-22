package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 草稿表
 * draft
 */
@Data
@TableName("draft")
public class Draft implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 摘要/简介
     */
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

    /**
     * 封面图URL
     */
    private String coverImage;

    /**
     * 模块类型:1文章/2问答/3笔记/4项目
     */
    private String moduleType;

    /**
     * 关联正式内容ID（编辑时）
     */
    private Long moduleId;

    /**
     * 分类ID
     */
    private Long categoryId;

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

    /**
     * 原文链接（转载时）
     */
    private String originalUrl;

    /**
     * 问题类型:技术/求职/生活等
     */
    private String questionType;

    /**
     * 悬赏积分
     */
    private Integer rewardPoints;

    /**
     * 最佳答案ID
     */
    private Long bestAnswerId;

    /**
     * 项目类型:开源/商业/个人
     */
    private String projectType;

    /**
     * 技术栈，多个用逗号分隔
     */
    private String techStack;

    /**
     * GitHub地址
     */
    private String githubUrl;

    /**
     * 演示地址
     */
    private String demoUrl;

    /**
     * 项目状态:ongoing/completed/archived
     */
    private String projectStatus;

    /**
     * 字数
     */
    private Integer wordCount;

    /**
     * 阅读时长（分钟）
     */
    private Integer readingTime;

    /**
     * 字符数
     */
    private Integer charCount;

    /**
     * 图片数量
     */
    private Integer imageCount;

    /**
     * 代码块数量
     */
    private Integer codeBlockCount;

    /**
     * 是否自动保存 0:否 1:是
     */
    private String autoSaved;

    /**
     * 是否临时草稿 0:否 1:是
     */
    private String isTemporary;

    /**
     * 临时草稿过期时间
     */
    private Date tempExpireAt;

    /**
     * 保存原因:auto/timed/manual
     */
    private String saveReason;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 父草稿ID
     */
    private Long parentDraftId;

    /**
     * 版本变更说明
     */
    private String changeLog;

    /**
     * 最后修改时间
     */
    private Date lastModifiedAt;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 额外数据，存储编辑器状态、自定义设置等
     */
    private String extraData;

    private static final long serialVersionUID = 1L;
}