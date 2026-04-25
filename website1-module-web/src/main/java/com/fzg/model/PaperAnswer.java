package com.fzg.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 答卷题目结果
 */
@Data
@TableName("paper_answer")
public class PaperAnswer {
    private Long attemptId;
    private Long questionId;
    private String userAnswer; // JSON
    private Integer score;
    private Integer spendSeconds;
}
