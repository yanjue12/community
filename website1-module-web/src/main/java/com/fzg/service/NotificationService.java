package com.fzg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.mapper.Notificationmapper;
import com.fzg.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 通知服务 - 增强版本
 * 支持缓存、批量操作、并发控制
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final Notificationmapper notificationMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String UNREAD_COUNT_KEY = "notification:unread:";
    private static final String UNREAD_TYPE_KEY = "notification:unread:type:";
    private static final long CACHE_EXPIRE_TIME = 3600; // 1小时

    /**
     * 获取未读数量（带缓存）
     */
    public Long getUnreadCount(Long userId) {
        String cacheKey = UNREAD_COUNT_KEY + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Long.parseLong(cached.toString());
        }

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, "0")
               .eq(Notification::getIsDeleted, "0");
        Long count = notificationMapper.selectCount(wrapper);

        redisTemplate.opsForValue().set(cacheKey, count, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        return count;
    }

    /**
     * 获取各类型未读数量
     */
    public Map<String, Long> getUnreadCountByType(Long userId) {
        String cacheKey = UNREAD_TYPE_KEY + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (Map<String, Long>) cached;
        }

        Map<String, Long> result = new HashMap<>();
        String[] types = {"user", "system", "message"};
        
        for (String type : types) {
            LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Notification::getUserId, userId)
                   .eq(Notification::getType, type)
                   .eq(Notification::getIsRead, "0")
                   .eq(Notification::getIsDeleted, "0");
            Long count = notificationMapper.selectCount(wrapper);
            result.put(type, count);
        }

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        return result;
    }

    /**
     * 标记单个为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long userId, Long notificationId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getId, notificationId)
               .eq(Notification::getIsRead, "0");

        Notification update = new Notification();
        update.setIsRead("1");
        update.setReadAt(new Date());

        int result = notificationMapper.update(update, wrapper);
        if (result > 0) {
            invalidateCache(userId);
        }
        return result > 0;
    }

    /**
     * 批量标记为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public int markBatchAsRead(Long userId, List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .in(Notification::getId, notificationIds)
               .eq(Notification::getIsRead, "0");

        Notification update = new Notification();
        update.setIsRead("1");
        update.setReadAt(new Date());

        int result = notificationMapper.update(update, wrapper);
        if (result > 0) {
            invalidateCache(userId);
        }
        return result;
    }

    /**
     * 全部标记为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public int markAllAsRead(Long userId, String type) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, "0")
               .eq(Notification::getIsDeleted, "0");
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }

        Notification update = new Notification();
        update.setIsRead("1");
        update.setReadAt(new Date());

        int result = notificationMapper.update(update, wrapper);
        if (result > 0) {
            invalidateCache(userId);
        }
        return result;
    }

    /**
     * 删除通知
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotification(Long userId, Long notificationId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getId, notificationId);

        Notification update = new Notification();
        update.setIsDeleted("1");

        int result = notificationMapper.update(update, wrapper);
        if (result > 0) {
            invalidateCache(userId);
        }
        return result > 0;
    }

    /**
     * 批量删除通知
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteBatch(Long userId, List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .in(Notification::getId, notificationIds);

        Notification update = new Notification();
        update.setIsDeleted("1");

        int result = notificationMapper.update(update, wrapper);
        if (result > 0) {
            invalidateCache(userId);
        }
        return result;
    }

    /**
     * 清空已读通知
     */
    @Transactional(rollbackFor = Exception.class)
    public int clearReadNotifications(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, "1")
               .eq(Notification::getIsDeleted, "0");

        Notification update = new Notification();
        update.setIsDeleted("1");

        int result = notificationMapper.update(update, wrapper);
        if (result > 0) {
            invalidateCache(userId);
        }
        return result;
    }

    /**
     * 获取通知列表（分页）
     */
    public Page<Notification> getNotificationList(Long userId, Integer pageNum, Integer pageSize, String type, String isRead) {
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);

        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsDeleted, "0")
               .orderByDesc(Notification::getCreatedAt);

        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        if (isRead != null && !isRead.isEmpty()) {
            wrapper.eq(Notification::getIsRead, isRead);
        }

        return notificationMapper.selectPage(page, wrapper);
    }

    /**
     * 获取通知详情（自动标记为已读）
     */
    @Transactional(rollbackFor = Exception.class)
    public Notification getNotificationDetail(Long userId, Long notificationId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getId, notificationId)
               .eq(Notification::getIsDeleted, "0");

        Notification notification = notificationMapper.selectOne(wrapper);
        if (notification != null && "0".equals(notification.getIsRead())) {
            markAsRead(userId, notificationId);
        }
        return notification;
    }

    /**
     * 清除缓存
     */
    private void invalidateCache(Long userId) {
        redisTemplate.delete(UNREAD_COUNT_KEY + userId);
        redisTemplate.delete(UNREAD_TYPE_KEY + userId);
    }
}
