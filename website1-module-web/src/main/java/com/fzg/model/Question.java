package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 题库表
 */
@Data
@TableName("question")
public class Question {

    /** 题目ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 1单选 2多选 3问答 */
    private Integer type;

    /** 题干（Markdown） */
    private String content;

    /** 选项(JSON数组) */
    private String options;

    /** 标准答案(JSON) */
    private String answer;

    /** 解析 */
    private String explanation;

    /** 标签/知识点(JSON数组) */
    private String tags;

    /** 难度 1-5 */
    private Integer difficulty;

    private Date createdAt;
    private Date updatedAt;
}
