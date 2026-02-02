package com.fzg.constant;

public class RedisFollowKey {
    // 我关注的人（following 列表）
    public static String followingSet(Long userId) {
        return "follow:following:" + userId;
    }

    // 关注我的人（follower 列表）
    public static String followerSet(Long userId) {
        return "follow:follower:" + userId;
    }

    // 防并发锁
    public static String getFollowLockKey(Long followerId, Long followingId) {
        return String.format("follow:lock:%d:%d", followerId, followingId);
    }

    // 关注状态缓存（可选，但结构先给你）
    public static String getFollowStatusKey(Long followerId, Long followingId) {
        return String.format("follow:status:%d:%d", followerId, followingId);
    }
}
