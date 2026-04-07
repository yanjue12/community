package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.constant.RedisArticleKey;
import com.fzg.enums.BehaviorTypeEnum;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.LikeRecordMapper;
import com.fzg.mapper.UserMapper;
import com.fzg.model.Article;
import com.fzg.model.LikeRecord;
import com.fzg.model.Result;
import com.fzg.model.User;
import com.fzg.ratelimit.LikeRateLimit;
import com.fzg.service.LikeRecordService;
import com.fzg.service.NotificationPublisher;
import com.fzg.service.UserBehaviorService;
import com.fzg.vo.LikeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements LikeRecordService {

    private final RedisTemplate<String, String> redisTemplate;
    private final Articlemapper articlemapper;
    private final UserMapper userMapper;
    private final LikeRateLimit likeRateLimit;
    private final NotificationPublisher notificationPublisher;
    private final UserBehaviorService userBehaviorService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result articleLike(LikeRequest likeRequest) {

        Integer actionLike = likeRequest.getActionLike();
        Long userId = likeRequest.getUserId();
        Long articleId = likeRequest.getArticleId();

        if(!likeRateLimit.checkUserRateLimit(userId)){
            return Result.fail(EnumReturn.OPERATION_TOO_FREQUENTLY);
        }


        String lockKey = RedisArticleKey.getLikeArticleLockKey(userId, articleId);
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 3, TimeUnit.SECONDS);
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
                    oldStatus = "0";
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
                articlemapper.upArticleLikeCount(articleId, actionLike,"like");
                
                // 点赞时发送通知
                if (actionLike == 1) {
                    Article article = articlemapper.selectById(articleId);
                    User liker = userMapper.selectById(userId);
                    if (article != null && liker != null) {
                        String likerName = liker.getNickname() != null ? liker.getNickname() : "匿名用户";
                        notificationPublisher.publishArticleLikeNotification(
                                article.getUserId(), userId, articleId, article.getTitle(), likerName
                        );
                    }

                    userBehaviorService.recordBehavior(
                            userId,
                            articleId,
                            BehaviorTypeEnum.LIKE
                    );
                }
            }
            // 关键：更新 Redis 缓存点赞状态
            String cacheKey = RedisArticleKey.getLikeArticleStatusKey(userId, articleId);
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



}
