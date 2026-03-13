package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.constant.RedisArticleKey;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.Favoritemapper;
import com.fzg.mapper.UserMapper;
import com.fzg.model.Article;
import com.fzg.model.Favorite;
import com.fzg.model.Result;
import com.fzg.model.User;
import com.fzg.ratelimit.LikeRateLimit;
import com.fzg.service.FavoriteService;
import com.fzg.service.NotificationPublisher;
import com.fzg.vo.LikeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FavoriteServiceImpl extends ServiceImpl<Favoritemapper, Favorite> implements FavoriteService {


    @Autowired
    private LikeRateLimit likeRateLimit;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private Articlemapper articlemapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotificationPublisher notificationPublisher;

    @Override
    public Result articleCollect(LikeRequest likeRequest) {

        Integer actionLike = likeRequest.getActionLike();
        Long userId = likeRequest.getUserId();
        Long articleId = likeRequest.getArticleId();

        if(!likeRateLimit.checkUserFavoriteRateLimit(userId)){
            return Result.fail(EnumReturn.OPERATION_TOO_FREQUENTLY);
        }


        String lockKey = RedisArticleKey.getFavoriteArticleLockKey(userId, articleId);
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 3, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return Result.success(true);
        }
        try {
            Favorite record = getOne(
                    new LambdaQueryWrapper<Favorite>()
                            .eq(Favorite::getUserId, userId)
                            .eq(Favorite::getTargetId, articleId)
                            .eq(Favorite::getTargetType, likeRequest.getType())
            );

            String oldStatus = null;
            // 没有记录 第一次收藏 新增
            if (record == null) {
                if (actionLike == 1) {
                    Favorite r = new Favorite();
                    r.setUserId(userId);
                    r.setTargetId(articleId);
                    r.setTargetType(likeRequest.getType());
                    r.setStatus(String.valueOf(actionLike));
                    save(r);
                    oldStatus = "0";
                } else {//用户取消收藏，，但是实际没有收藏，直接返回成功
                    return Result.success(true);
                }

            } else {
                //有数据，取消收藏或者收藏
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
                //文章收藏数修改
                articlemapper.upArticleLikeCount(articleId, actionLike,"favorite");
                
                // 收藏时发送通知
                if (actionLike == 1) {
                    Article article = articlemapper.selectById(articleId);
                    User collector = userMapper.selectById(userId);
                    if (article != null && collector != null) {
                        String collectorName = collector.getNickname() != null ? collector.getNickname() : "匿名用户";
                        notificationPublisher.publishArticleCollectNotification(
                                article.getUserId(), userId, articleId, article.getTitle(), collectorName
                        );
                    }
                }
            }
            // 关键：更新 Redis 缓存点赞状态
            String cacheKey = RedisArticleKey.getFavoriteArticleStatusKey(userId, articleId);
            if (actionLike == 1) {
                redisTemplate.opsForValue().set(cacheKey, "1", 7, TimeUnit.DAYS);
            } else {
                redisTemplate.delete(cacheKey);
            }
        } catch (Exception e) {
            log.error("文章收藏数修改失败:{}", e);
            return Result.fail(EnumReturn.valueOf("文章收藏数修改失败"));
        } finally {
            redisTemplate.delete(lockKey);
        }
        return Result.success(true);
    }
}
