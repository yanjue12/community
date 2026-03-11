package com.fzg.service;

import com.fzg.mapper.Notificationmapper;
import com.fzg.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 批量通知服务
 * 用于处理大量通知的场景（如新文章推送给所有粉丝）
 * 支持分批处理，避免一次性插入过多数据导致性能问题
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BatchNotificationService {

    private final Notificationmapper notificationMapper;
    private static final int BATCH_SIZE = 500; // 每批处理500条

    /**
     * 批量创建通知
     * 异步处理，分批插入数据库
     */
    @Async("batchNotificationExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateNotifications(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        int total = notifications.size();
        int processed = 0;

        try {
            // 分批处理
            for (int i = 0; i < total; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, total);
                List<Notification> batch = notifications.subList(i, end);
                
                // 批量插入
                for (Notification notification : batch) {
                    notificationMapper.insert(notification);
                }
                
                processed = end;
                log.debug("批量通知处理进度: {}/{}", processed, total);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("批量通知处理完成: 总数={}, 耗时={}ms", total, duration);
        } catch (Exception e) {
            log.error("批量通知处理失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送新文章给所有粉丝
     * 用于作者发布新文章时，通知所有粉丝
     */
    @Async("batchNotificationExecutor")
    public void notifyFollowersNewArticle(Long authorId, List<Long> followerIds, 
                                         Long articleId, String articleTitle) {
        if (followerIds == null || followerIds.isEmpty()) {
            return;
        }

        List<Notification> notifications = followerIds.stream()
                .map(followerId -> {
                    Notification notification = new Notification();
                    notification.setUserId(followerId);
                    notification.setFromUserId(authorId);
                    notification.setType("user");
                    notification.setActionType("new_article");
                    notification.setTitle("关注的作者发布新文章");
                    notification.setContent("发布了新文章《" + articleTitle + "》");
                    notification.setTargetType("article");
                    notification.setTargetId(articleId);
                    notification.setGroupId("new_article_" + articleId);
                    notification.setIsRead("0");
                    notification.setIsDeleted("0");
                    notification.setNotifyLevel("normal");
                    notification.setCreatedAt(new Date());
                    notification.setExtraData("{\"articleTitle\":\"" + articleTitle + "\"}");
                    return notification;
                })
                .collect(Collectors.toList());

        batchCreateNotifications(notifications);
        log.info("新文章推送完成: authorId={}, articleId={}, followerCount={}", 
                authorId, articleId, followerIds.size());
    }

    /**
     * 系统通知所有用户
     */
    @Async("batchNotificationExecutor")
    public void notifyAllUsers(List<Long> userIds, String title, String content, String level) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        List<Notification> notifications = userIds.stream()
                .map(userId -> {
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
                    return notification;
                })
                .collect(Collectors.toList());

        batchCreateNotifications(notifications);
        log.info("系统通知发送完成: userCount={}, title={}", userIds.size(), title);
    }
}
