package com.fzg.ratelimit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LikeRateLimit {

    @Autowired
    private static RedisTemplate redisTemplate;

    public static boolean checkUserRateLimit(Long userId) {
        String key = "like:rate:user:" + userId;
        long now = System.currentTimeMillis();

        // 1. 移除 10 秒前的记录
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, now - 10_000);

        // 2. 当前次数
        Long count = redisTemplate.opsForZSet().zCard(key);
        if (count != null && count >= 20) {
            return false;
        }

        // 3. 记录本次请求
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
        redisTemplate.expire(key, 15, TimeUnit.SECONDS);

        return true;
    }

    public static boolean checkIpRateLimit(String ip) {
        String key = "like:rate:ip:" + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.SECONDS);
        }
        return count <= 20;
    }

    public static boolean checkUserArticleLimit(Long userId, Long articleId) {
        String key = "like:rate:user_article:" + userId + ":" + articleId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, 5, TimeUnit.SECONDS);
        }
        return count <= 3;
    }

    public static boolean checkRisk(Long userId) {
        String key = "like:risk:user:" + userId;
        Integer score = (Integer) redisTemplate.opsForValue().get(key);
        return score == null || score < 10;
    }


}
