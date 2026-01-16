package com.fzg.constant;

public class RedisKeyManager {
    
    // 用户点赞状态 key: like:status:{userId}:{articleId}
    public static String getUserLikeStatusKey(Long userId, Long articleId) {
        return String.format("like:status:%d:%d", userId, articleId);
    }
    
    // 文章点赞数 key: like:count:{articleId}
    public static String getArticleLikeCountKey(Long articleId) {
        return String.format("like:count:%d", articleId);
    }
    
    // 用户分布式锁 key: like:lock:{userId}:{articleId}
    public static String getLikeLockKey(Long userId, Long articleId) {
        return String.format("like:lock:%d:%d", userId, articleId);
    }
    
    // 脏数据集合 key: like:dirty:set
    public static String getDirtySetKey() {
        return "like:dirty:set";
    }
    
    // 需要同步的点赞记录集合 key: like:sync:record:set
    public static String getSyncRecordSetKey() {
        return "like:sync:record:set";
    }
}