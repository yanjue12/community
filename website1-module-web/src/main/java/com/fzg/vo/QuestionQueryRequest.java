package com.fzg.vo;

import lombok.Data;

@Data
    public  class QuestionQueryRequest {
        private Integer pageNum;
        private Integer pageSize;
        private String keyword;
        private Integer type;
        private Integer difficulty;
        private String tag;
    }