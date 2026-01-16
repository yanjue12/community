package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.constant.RedisLikeArticleKey;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.LikeRecordMapper;
import com.fzg.model.LikeRecord;
import com.fzg.model.Result;
import com.fzg.service.LikeRecordService;
import com.fzg.vo.LikeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements LikeRecordService {

    private final RedisTemplate<String, String> redisTemplate;
    private final Articlemapper articlemapper;


    @Override
    public Result articleLike(LikeRequest likeRequest) {

        Integer actionLike = likeRequest.getActionLike();
        Long userId = likeRequest.getUserId();
        Long articleId = likeRequest.getArticleId();

        String lockKey = RedisLikeArticleKey.getLikeArticleLockKey(userId, articleId);
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 1, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return Result.success(true);
        }
        try {

            LikeRecord record = getOne(
                    new LambdaQueryWrapper<LikeRecord>()
                            .eq(LikeRecord::getUserId, userId)
                            .eq(LikeRecord::getArticleId, articleId)
                            .eq(LikeRecord::getArticleType, likeRequest.getType())
            );

            String oldStatus = null;
            // 没有记录 第一次点赞 新增
            if (record == null) {
                if (actionLike == 1) {
                    LikeRecord r = new LikeRecord();
                    r.setUserId(userId);
                    r.setArticleId(articleId);
                    r.setArticleType(likeRequest.getType());
                    r.setStatus(String.valueOf(actionLike));
                    save(r);
                } else {//用户取消点赞，，但是实际没有点赞，直接返回成功
                    return Result.success(true);
                }

            } else {
                //有数据，取消点赞或者点赞
                oldStatus = record.getStatus();
                String newStatus = String.valueOf(actionLike);
                if (oldStatus.equals(newStatus)) {
                    return Result.success(true);
                }
                record.setStatus(newStatus);
                updateById(record);
            }
            //只有状态真正改变 才更新文章数据库
            if (oldStatus != null && !oldStatus.equals(String.valueOf(actionLike))) {
                //文章点赞数修改
                articlemapper.upArticleLikeCount(articleId, actionLike);
            }
            // 关键：更新 Redis 缓存点赞状态
            String cacheKey = RedisLikeArticleKey.getLikeArticleStatusKey(userId, articleId);
            if (actionLike == 1) {
                redisTemplate.opsForValue().set(cacheKey, "1", 7, TimeUnit.DAYS);
            } else {
                redisTemplate.delete(cacheKey);
            }
        } catch (Exception e) {
            log.error("文章点赞数修改失败:{}", e);
            return Result.fail(EnumReturn.valueOf("文章点赞数修改失败"));
        } finally {
            redisTemplate.delete(lockKey);
        }
        return Result.success(true);
    }

    public boolean checkUserRateLimit(Long userId) {
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


}
