package com.fzg.vo;

import lombok.Data;

import java.util.List;

@Data
public class ResultSearchVO {
    List<ArticleVO> articles;
    List<UserVO> users;
    List<ArticleVO> articlesByTag;
}
