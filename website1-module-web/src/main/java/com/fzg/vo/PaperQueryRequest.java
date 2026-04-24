package com.fzg.vo;

import lombok.Data;

@Data
public class PaperQueryRequest {
    private Integer pageNum;
    private Integer pageSize;
    private String keyword;
    private Integer status;
}
