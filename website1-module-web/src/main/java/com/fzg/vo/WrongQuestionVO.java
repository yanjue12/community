package com.fzg.vo;

import lombok.Data;

import java.util.Date;

/**
 * 错题本展示 VO（关联 question/paper 字段一并返回）
 */
@Data
public class WrongQuestionVO {
    private Long id;
    private Long userId;
    private Long questionId;
    private Long paperId;
    private Long attemptId;

    /** 题型 1单选 2多选 3问答 */
    private Integer type;
    /** 题干 */
    private String content;
    /** 选项 JSON */
    private String options;
    /** 标准答案 JSON */
    private String answer;
    /** 解析 */
    private String explanation;
    /** 知识点标签 JSON */
    private String tags;
    /** 难度 */
    private Integer difficulty;

    /** 来源试卷标题 */
    private String paperTitle;

    /** 用户最近一次错误答案 */
    private String lastUserAnswer;
    /** 累计错误次数 */
    private Integer wrongCount;
    /** 是否已掌握 0未 1已 */
    private Integer mastered;
    private Date masteredAt;
    private Date lastWrongAt;
    private Date createdAt;
    private Date updatedAt;
}
