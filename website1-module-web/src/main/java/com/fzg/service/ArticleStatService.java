package com.fzg.service;

public interface ArticleStatService {
    void handleViewCount(Long articleId, Long userId, String ip,Long authorId);
    void recordReadTime(Long articleId, Long userId, Integer duration, String ip);
}
