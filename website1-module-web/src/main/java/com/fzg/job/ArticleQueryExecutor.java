package com.fzg.job;

import com.fzg.vo.ArticleVO;

import java.util.List;

@FunctionalInterface
public interface ArticleQueryExecutor {
    List<ArticleVO> execute(Long userId, Integer pageSize, Integer offset);
}
