package com.fzg.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 试卷题目关联
 */
@Data
@TableName("paper_question")
public class PaperQuestion {
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @TableId
    private Long id; // 可用自增或组合主键，简化为单列ID
    private Long paperId;
    private Long questionId;
    private Integer seq;
    private Integer score;
}
