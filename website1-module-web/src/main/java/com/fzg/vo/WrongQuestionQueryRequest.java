package com.fzg.vo;

import lombok.Data;

@Data
public class WrongQuestionQueryRequest {
    private Integer pageNum;
    private Integer pageSize;
    /** 用户ID（必填） */
    private Long userId;
    /** 题型 1单选 2多选 3问答 */
    private Integer type;
    /** 难度 1-5 */
    private Integer difficulty;
    /** 知识点/标签模糊 */
    private String tag;
    /** 关键词，匹配题干 */
    private String keyword;
    /** 来源试卷ID */
    private Long paperId;
    /** 是否已掌握 0未 1已 null=全部 */
    private Integer mastered;
}
