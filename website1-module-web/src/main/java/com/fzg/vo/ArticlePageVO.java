package com.fzg.vo;

import lombok.Data;

import java.util.List;

@Data
public class ArticlePageVO {
    List<ArticleVO> articleVOList;
    Integer total;
}
