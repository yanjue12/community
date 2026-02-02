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

    /**
     * 登录用户文章阅读浏览量
     */
    public static String getArtViewCountKeyId(Long articleId,Long userId) {
        return String.format("article:view:count:%d:%d", articleId, userId);
    }

    /**
     *游客文章阅读浏览量
     */
    public static String getArtViewCountKeyIp(String ip,Long articleId) {
        return String.format("article:view:count:%d:%s",articleId, ip);
    }


    public static String readTimeUid(Long articleId, Long userId){
        return String.format("art:read:uid:%d:%d", articleId, userId);
    }

    public static String readTimeIp(Long articleId, String ip){
        return String.format("art:read:ip:%d:%s", articleId, ip);
    }



}
