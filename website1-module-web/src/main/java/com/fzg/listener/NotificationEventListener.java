package com.fzg.listener;

import com.alibaba.fastjson.JSON;
import com.fzg.event.NotificationEvent;
import com.fzg.mapper.Notificationmapper;
import com.fzg.model.Notification;
import com.fzg.service.WebSocketPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 通知事件监听器 - 异步处理通知
 * 使用@Async注解实现异步处理，提高系统吞吐量
 * 集成WebSocket实时推送功能
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {

    private final Notificationmapper notificationMapper;
    private final WebSocketPushService webSocketPushService;

    /**
     * 监听通知事件，异步保存到数据库并推送给在线用户
     */
    @EventListener
    @Async("notificationExecutor")
    public void onNotificationEvent(NotificationEvent event) {
        try {
            // 不给自己发通知
            if (event.getUserId() != null && event.getFromUserId() != null && 
                event.getUserId().equals(event.getFromUserId())) {
                return;
            }

            // 1. 保存通知到数据库
            Notification notification = new Notification();
            notification.setUserId(event.getUserId());
            notification.setFromUserId(event.getFromUserId());
            notification.setType(event.getType());
            notification.setActionType(event.getActionType());
            notification.setTitle(event.getTitle());
            notification.setContent(event.getContent());
            notification.setTargetType(event.getTargetType());
            notification.setTargetId(event.getTargetId());
            notification.setGroupId(event.getGroupId());
            notification.setIsRead("0");
            notification.setIsDeleted("0");
            notification.setNotifyLevel("normal");
            notification.setCreatedAt(new Date());

            // 处理额外数据
            if (event.getExtraData() != null && !event.getExtraData().isEmpty()) {
                notification.setExtraData(JSON.toJSONString(event.getExtraData()));
            }

            notificationMapper.insert(notification);
            log.debug("通知已保存到数据库: userId={}, actionType={}", event.getUserId(), event.getActionType());

            // 2. 如果用户在线，通过WebSocket推送实时通知
            boolean pushed = webSocketPushService.pushNotificationToUser(
                    event.getUserId(),
                    event.getActionType(),
                    event.getTitle(),
                    event.getContent(),
                    event.getExtraData()
            );

            if (pushed) {
                log.debug("实时通知推送成功: userId={}, title={}", event.getUserId(), event.getTitle());
            } else {
                log.debug("用户不在线，仅保存到数据库: userId={}", event.getUserId());
            }

        } catch (Exception e) {
            log.error("处理通知事件失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响主业务流程
        }
    }
}
