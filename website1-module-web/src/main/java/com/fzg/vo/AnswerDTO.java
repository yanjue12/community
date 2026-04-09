package com.fzg.vo;

import lombok.Data;

@Data
public class AnswerDTO {
    private Long questionId;
    private Object answer; // 单选: String, 多选: List<String>, 问答: String
    private Integer spendSeconds;
}
