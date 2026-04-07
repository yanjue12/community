package com.fzg.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.Notificationmapper;
import com.fzg.mapper.UserMapper;
import com.fzg.model.Notification;
import com.fzg.model.User;
import com.fzg.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 通知服务 - 合并版本
 * 支持通知创建、缓存、批量操作、并发控制
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService extends ServiceImpl<Notificationmapper,Notification> implements INotificationService {

    private final Notificationmapper notificationMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserMapper userMapper;

    private static final String UNREAD_COUNT_KEY = "notification:unread:";
    private static final String UNREAD_TYPE_KEY = "notification:unread:type:";
    private static final long CACHE_EXPIRE_TIME = 10; // 10分钟

    // ==================== 缓存相关方法 ====================

    /**
     * 获取未读数量（带缓存）
     */
    @Override
    public Long getUnreadCount(Long userId) {
        String cacheKey = UNREAD_COUNT_KEY + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Long.parseLong(cached.toString());
        }

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, "0")
                .eq(Notification::getIsDeleted, "0")
                .notIn(Notification::getActionType, "user_login", "user_register");
        Long count = notificationMapper.selectCount(wrapper);

        redisTemplate.opsForValue().set(cacheKey,String.valueOf(count), CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        return count;
    }

    /**
     * 获取各类型未读数量
     */
    @Override
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
                    .eq(Notification::getIsDeleted, "0")
                    .notIn(Notification::getActionType, "user_login", "user_register");
            Long count = notificationMapper.selectCount(wrapper);
            result.put(type, count);
        }

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
        return result;
    }




    /**
     * 清除缓存
     */
    private void invalidateCache(Long userId) {
        redisTemplate.delete(UNREAD_COUNT_KEY + userId);
        redisTemplate.delete(UNREAD_TYPE_KEY + userId);
    }

    /**
     * 按 category 应用查询条件
     * like=点赞, comment=评论, follow=关注, system=系统通知, null/空=全部
     */
    private void applyCategoryFilter(LambdaQueryWrapper<Notification> wrapper, String category) {
        if (category == null || category.isEmpty()) {
            wrapper.notIn(Notification::getActionType, "user_login", "user_register");
            return;
        }
        switch (category) {
            case "like":
                wrapper.in(Notification::getActionType, "like_article", "like_comment");
                break;
            case "comment":
                wrapper.in(Notification::getActionType, "comment_article", "reply_comment", "mention");
                break;
            case "follow":
                wrapper.in(Notification::getActionType, "follow", "new_article");
                break;
            case "system":
                wrapper.eq(Notification::getType, "system")
                       .notIn(Notification::getActionType, "user_login", "user_register");
                break;
            default:
                wrapper.notIn(Notification::getActionType, "user_login", "user_register");
        }
    }

    /**
     * 批量填充发送人头像
     */
    private void fillFromUserAvatar(List<Notification> list) {
        if (list == null || list.isEmpty()) return;
        // 收集所有非空的 fromUserId
        Set<Long> fromUserIds = new java.util.HashSet<>();
        for (Notification n : list) {
            if (n.getFromUserId() != null) {
                fromUserIds.add(n.getFromUserId());
            }
        }
        if (fromUserIds.isEmpty()) return;
        // 批量查头像
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        q.in(User::getId, fromUserIds).select(User::getId, User::getAvatar);
        Map<Long, String> avatarMap = new java.util.HashMap<>();
        userMapper.selectList(q).forEach(u -> avatarMap.put(u.getId(), u.getAvatar()));
        // 回填
        for (Notification n : list) {
            if (n.getFromUserId() != null) {
                n.setFromUserAvatar(avatarMap.get(n.getFromUserId()));
            }
        }
    }

    // ==================== 通知创建方法 ====================

    /**
     * 创建通知（通用方法）
     */
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(
            Long userId,
            Long fromUserId,
            String type,
            String actionType,
            String title,
            String content,
            String targetType,
            Long targetId,
            String groupId,
            Object extraData
    ) {
        // 不给自己发通知
        if (userId.equals(fromUserId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setActionType(actionType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setGroupId(groupId);
        notification.setIsRead("0");
        notification.setIsDeleted("0");
        notification.setNotifyLevel("normal");
        notification.setCreatedAt(new Date());

        if (extraData != null) {
            notification.setExtraData(JSON.toJSONString(extraData));
        }

        notificationMapper.insert(notification);
        invalidateCache(userId);
    }

    /**
     * 文章被点赞通知
     */
    public void notifyArticleLike(Long authorId, Long likerId, Long articleId, String articleTitle) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("articleTitle", articleTitle);
        
        createNotification(
                authorId,
                likerId,
                "user",
                "like_article",
                "文章获得点赞",
                "赞了你的文章《" + articleTitle + "》",
                "article",
                articleId,
                "like_article_" + articleId,
                extra
        );
    }

    /**
     * 评论被点赞通知
     */
    public void notifyCommentLike(Long commentAuthorId, Long likerId, Long commentId, String commentContent) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("commentContent", commentContent);
        
        createNotification(
                commentAuthorId,
                likerId,
                "user",
                "like_comment",
                "评论获得点赞",
                "赞了你的评论",
                "comment",
                commentId,
                "like_comment_" + commentId,
                extra
        );
    }

    /**
     * 文章被评论通知
     */
    public void notifyArticleComment(Long authorId, Long commenterId, Long articleId, String articleTitle, Long commentId, String commentContent) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("articleTitle", articleTitle);
        extra.put("commentContent", commentContent);
        
        createNotification(
                authorId,
                commenterId,
                "user",
                "comment_article",
                "文章收到评论",
                "评论了你的文章《" + articleTitle + "》",
                "article",
                articleId,
                "comment_article_" + articleId,
                extra
        );
    }

    /**
     * 评论被回复通知
     */
    public void notifyCommentReply(Long commentAuthorId, Long replierId, Long parentCommentId, Long replyCommentId, String replyContent) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("replyContent", replyContent);
        extra.put("parentCommentId", parentCommentId);
        
        createNotification(
                commentAuthorId,
                replierId,
                "user",
                "reply_comment",
                "评论收到回复",
                "回复了你的评论",
                "comment",
                replyCommentId,
                "reply_comment_" + parentCommentId,
                extra
        );
    }

    /**
     * @提及通知
     */
    public void notifyMention(Long mentionedUserId, Long mentionerId, String contentType, Long contentId, String content) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("content", content);
        
        createNotification(
                mentionedUserId,
                mentionerId,
                "user",
                "mention",
                "有人@了你",
                "在" + contentType + "中提到了你",
                contentType,
                contentId,
                "mention_" + contentType + "_" + contentId,
                extra
        );
    }

    /**
     * 关注通知
     */
    public void notifyFollow(Long followedUserId, Long followerId) {
        createNotification(
                followedUserId,
                followerId,
                "user",
                "follow",
                "新增粉丝",
                "关注了你",
                "user",
                followerId,
                "follow_" + followerId,
                null
        );
    }

    /**
     * 文章被收藏通知
     */
    public void notifyArticleCollect(Long authorId, Long collectorId, Long articleId, String articleTitle) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("articleTitle", articleTitle);
        
        createNotification(
                authorId,
                collectorId,
                "user",
                "collect_article",
                "文章被收藏",
                "收藏了你的文章《" + articleTitle + "》",
                "article",
                articleId,
                "collect_article_" + articleId,
                extra
        );
    }

    /**
     * 文章被分享通知
     */
    public void notifyArticleShare(Long authorId, Long sharerId, Long articleId, String articleTitle) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("articleTitle", articleTitle);
        
        createNotification(
                authorId,
                sharerId,
                "user",
                "share_article",
                "文章被分享",
                "分享了你的文章《" + articleTitle + "》",
                "article",
                articleId,
                "share_article_" + articleId,
                extra
        );
    }

    /**
     * 关注的作者发布新文章通知
     */
    public void notifyFollowerNewArticle(Long followerId, Long authorId, Long articleId, String articleTitle) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("articleTitle", articleTitle);
        
        createNotification(
                followerId,
                authorId,
                "user",
                "new_article",
                "关注的作者发布新文章",
                "发布了新文章《" + articleTitle + "》",
                "article",
                articleId,
                "new_article_" + articleId,
                extra
        );
    }

    /**
     * 系统通知
     */
    public void notifySystem(Long userId, String title, String content, String level) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("system");
        notification.setActionType("system_notice");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead("0");
        notification.setIsDeleted("0");
        notification.setNotifyLevel(level != null ? level : "normal");
        notification.setCreatedAt(new Date());
        
        notificationMapper.insert(notification);
        invalidateCache(userId);
    }

    // ==================== 通知状态管理方法 ====================

    /**
     * 标记单个为已读
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
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
    @Override
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
    @Override
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
     * 按分类全部标记为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAllAsReadByCategory(Long userId, String category) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, "0")
               .eq(Notification::getIsDeleted, "0");

        applyCategoryFilter(wrapper, category);

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
     * 删除单个通知（逻辑删除）
     */
    @Override
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
     * 删除所有通知（逻辑删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAllNotifications(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
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
     * 批量删除通知
     */
    @Override
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
    @Override
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

    // ==================== 通知查询方法 ====================

    /**
     * 获取通知列表（分页）
     */
    @Override
    public Page<Notification> getNotificationList(Long userId, Integer pageNum, Integer pageSize, String type, String isRead) {
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);

        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsDeleted, "0")
               // 排除管理端监控用的登录/注册通知，这类通知不应展示给普通用户
               .notIn(Notification::getActionType, "user_login", "user_register")
               .orderByDesc(Notification::getCreatedAt);

        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        if (isRead != null && !isRead.isEmpty()) {
            wrapper.eq(Notification::getIsRead, isRead);
        }

        Page<Notification> result = notificationMapper.selectPage(page, wrapper);
        fillFromUserAvatar(result.getRecords());
        return result;
    }

    /**
     * 按分类获取通知列表（分页）
     * category: like=点赞(like_article/like_comment)
     *           comment=评论(comment_article/reply_comment/mention)
     *           follow=关注(follow/new_article)
     *           system=系统通知(type=system，排除user_login/user_register)
     */
    @Override
    public Page<Notification> getNotificationListByCategory(Long userId, Integer pageNum, Integer pageSize, String category, String isRead) {
        pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);

        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsDeleted, "0")
               .orderByDesc(Notification::getCreatedAt);

        applyCategoryFilter(wrapper, category);

        if (isRead != null && !isRead.isEmpty()) {
            wrapper.eq(Notification::getIsRead, isRead);
        }

        Page<Notification> result = notificationMapper.selectPage(page, wrapper);
        fillFromUserAvatar(result.getRecords());
        return result;
    }

    /**
     * 获取通知详情（自动标记为已读）
     */
    @Override
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
     * 手动清理已读通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int manualCleanupReadNotifications(Integer days) {
        if (days == null || days < 1) {
            days = 15; // 默认15天
        }
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        String cutoffTime = cutoffDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        log.info("手动清理{}天前的已读通知，截止时间: {}", days, cutoffTime);
        
        int deletedCount = notificationMapper.physicalDeleteReadNotifications(cutoffTime);
        
        log.info("手动清理完成，共删除{}条记录", deletedCount);
        
        return deletedCount;
    }
}
