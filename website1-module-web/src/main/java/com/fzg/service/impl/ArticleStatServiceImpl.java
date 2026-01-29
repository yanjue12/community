package com.fzg.service.impl;

import com.fzg.constant.RedisArticleKey;
import com.fzg.mapper.Articlemapper;
import com.fzg.model.Article;
import com.fzg.service.ArticleStatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ArticleStatServiceImpl implements ArticleStatService {

    @Autowired
    private Articlemapper articleMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public void handleViewCount(Long articleId, Long userId, String ip,Long authorId) {
        // 作者本人访问，直接 return
        if (userId != null && userId.equals(authorId)) {
            return;
        }
        String redisKey = (userId != null)
                ? RedisArticleKey.getArtViewCountKeyId(articleId, userId)
                : RedisArticleKey.getArtViewCountKeyIp(ip, articleId);

        // SETNX：不存在才设置
        Boolean firstView = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", 3, TimeUnit.MINUTES);

        if (Boolean.TRUE.equals(firstView)) {
            // 只有第一次访问才 +1
            articleMapper.incrViewCount(articleId);
        }
        System.out.println("切面逻辑handleViewCount");
        log.info("handleViewCount 切面逻辑");
    }

    @Override
    public void recordReadTime(Long articleId, Long userId, Integer duration, String ip) {

        // 防刷：低于 3 秒直接丢弃
        if (duration == null || duration < 3) {
            return;
        }

        String key = (userId != null)
                ? RedisArticleKey.getArtViewCountKeyId(articleId, userId)
                : RedisArticleKey.getArtViewCountKeyIp(ip, articleId);

        // 累加阅读时长
        redisTemplate.opsForValue().increment(key, duration);

        // 统一设置 24h 过期
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
        log.info("recordReadTime切面逻辑");
    }

}
