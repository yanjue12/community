package com.fzg.service.impl;

import com.fzg.enums.BehaviorTypeEnum;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.UserBehaviorLogMapper;
import com.fzg.model.ArticleDimension;
import com.fzg.model.UserBehaviorLog;
import com.fzg.service.UserBehaviorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class UserBehaviorServiceImpl implements UserBehaviorService {

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    @Autowired
    private Articlemapper articleMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordBehavior(
            Long userId,
            Long articleId,
            BehaviorTypeEnum behaviorType) {

        //未登录只统计浏览量，不进画像
        if (userId == null) {
            if (behaviorType == BehaviorTypeEnum.VIEW) {
                articleMapper.incrViewCount(articleId);
            }
            return;
        }

        //1.防刷校验
        if (!checkAndLockBehavior(userId, articleId, behaviorType)) {
            return;
        }

        //2.查文章维度（画像用）
        ArticleDimension dim = articleMapper.selectArticleDimension(articleId);
        if (dim == null) {
            return;
        }

        //3.写行为日志
        UserBehaviorLog log = new UserBehaviorLog();
        log.setUserId(userId);
        log.setArticleId(articleId);
        log.setBehaviorType(behaviorType.getCode());
        log.setBehaviorWeight(behaviorType.getBaseWeight());
        log.setTagId(dim.getTagId());
        log.setCategoryId(dim.getCategoryId());
        log.setAuthorId(dim.getAuthorId());
        log.setCreateAt(LocalDateTime.now());

        behaviorLogMapper.insert(log);


        //5.标记用户画像需要更新（轻量）
        markProfileDirty(userId);
    }



    private boolean checkAndLockBehavior(
            Long userId,
            Long articleId,
            BehaviorTypeEnum type) {

        String key = String.format(
                "user:behavior:%d:%d:%d",
                userId, articleId, type.getCode()
        );

        long ttl;
        switch (type) {
            case VIEW:
                ttl = 10 * 60; // 浏览：10分钟算一次
                break;
            case LIKE:
            case COLLECT:
                ttl = 24 * 60 * 60; // 一天只能算一次
                break;
            case COMMENT:
                ttl = 60; // 评论防刷
                break;
            default:
                ttl = 300;
        }

        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", ttl, TimeUnit.SECONDS);

        return Boolean.TRUE.equals(ok);
    }


    private void markProfileDirty(Long userId) {
        redisTemplate.opsForSet()
                .add("user:profile:dirty", userId.toString());
    }




}
