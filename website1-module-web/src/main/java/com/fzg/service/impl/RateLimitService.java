package com.fzg.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    // 防刷配置
    private static final int USER_LIMIT = 10;      // 用户每分钟最多点赞次数
    private static final int IP_LIMIT = 30;        // IP每分钟最多点赞次数
    private static final int ARTICLE_LIMIT = 100;  // 文章每分钟最多被点赞次数
    private static final int WINDOW_SECONDS = 60;  // 时间窗口（秒）
    
    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 检查是否允许点赞
     */
    public boolean canLike(Long userId, Long articleId, String ip) {
        // 1. 用户级限制
        String userKey = String.format("rate:user:%d:minute", userId);
        Long userCount = redisTemplate.opsForValue().increment(userKey);
        if (userCount == 1) {
            redisTemplate.expire(userKey, WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        if (userCount > USER_LIMIT) {
            return false;
        }
        
        // 2. IP级限制
        String ipKey = String.format("rate:ip:%s:minute", ip);
        Long ipCount = redisTemplate.opsForValue().increment(ipKey);
        if (ipCount == 1) {
            redisTemplate.expire(ipKey, WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        if (ipCount > IP_LIMIT) {
            return false;
        }
        
        // 3. 文章级限制
        String articleKey = String.format("rate:article:%d:minute", articleId);
        Long articleCount = redisTemplate.opsForValue().increment(articleKey);
        if (articleCount == 1) {
            redisTemplate.expire(articleKey, WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        if (articleCount > ARTICLE_LIMIT) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 滑动窗口检查（更精确）
     */
    public boolean canLikeSlidingWindow(Long userId, Long articleId, String ip) {
        long currentTime = System.currentTimeMillis();
        long windowMillis = WINDOW_SECONDS * 1000L;
        long windowStart = currentTime - windowMillis;
        
        // 使用Sorted Set记录时间戳
        String userZSetKey = String.format("rate:user:%d:timestamps", userId);
        String ipZSetKey = String.format("rate:ip:%s:timestamps", ip);
        String articleZSetKey = String.format("rate:article:%d:timestamps", articleId);
        
        // 清理旧记录
        redisTemplate.opsForZSet().removeRangeByScore(userZSetKey, 0, windowStart);
        redisTemplate.opsForZSet().removeRangeByScore(ipZSetKey, 0, windowStart);
        redisTemplate.opsForZSet().removeRangeByScore(articleZSetKey, 0, windowStart);
        
        // 检查数量
        Long userCount = redisTemplate.opsForZSet().zCard(userZSetKey);
        Long ipCount = redisTemplate.opsForZSet().zCard(ipZSetKey);
        Long articleCount = redisTemplate.opsForZSet().zCard(articleZSetKey);
        
        if (userCount >= USER_LIMIT || ipCount >= IP_LIMIT || articleCount >= ARTICLE_LIMIT) {
            return false;
        }
        
        // 添加当前时间戳
        redisTemplate.opsForZSet().add(userZSetKey, String.valueOf(currentTime), currentTime);
        redisTemplate.opsForZSet().add(ipZSetKey, String.valueOf(currentTime), currentTime);
        redisTemplate.opsForZSet().add(articleZSetKey, String.valueOf(currentTime), currentTime);
        
        // 设置过期时间
        redisTemplate.expire(userZSetKey, WINDOW_SECONDS + 10, TimeUnit.SECONDS);
        redisTemplate.expire(ipZSetKey, WINDOW_SECONDS + 10, TimeUnit.SECONDS);
        redisTemplate.expire(articleZSetKey, WINDOW_SECONDS + 10, TimeUnit.SECONDS);
        
        return true;
    }
}