package com.fzg.constant;

public class RedisArticleKey {

    public static final String DIRTY_SET = "article:like:dirty:set";
    public static String getLikeArticleLockKey(Long userId, Long articleId) {
        return String.format("like:lock:%d:%d", userId, articleId);
    }

    public static String getLikeArticleStatusKey(Long userId, Long articleId) {
        return String.format("like:status:%d:%d", userId, articleId);
    }

    public static String getLikeArticleCountKey(Long articleId) {
        return String.format("article:like:count:%d", articleId);
    }

    public static String getArticleLikeDirtySetKey(Long articleId, Long userId) {
        return String.format("article:like:count:%d:%d", articleId, userId);
    }

    public static String getFavoriteArticleLockKey(Long userId, Long articleId) {
        return String.format("favorite:lock:%d:%d", userId, articleId);
    }
    public static String getFavoriteArticleStatusKey(Long userId, Long articleId) {
        return String.format("favorite:status:%d:%d", userId, articleId);
    }
    public static String getFavoriteArticleCountKey(Long articleId) {
        return String.format("article:favorite:count:%d", articleId);
    }
}
