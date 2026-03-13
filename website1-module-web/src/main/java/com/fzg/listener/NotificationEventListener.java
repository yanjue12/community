package com.fzg.listener;

import com.alibaba.fastjson.JSON;
import com.fzg.event.NotificationEvent;
import com.fzg.mapper.Notificationmapper;
import com.fzg.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 通知事件监听器 - 异步处理通知
 * 使用@Async注解实现异步处理，提高系统吞吐量
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {

    private final Notificationmapper notificationMapper;

    /**
     * 监听通知事件，异步保存到数据库
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
            log.debug("通知已保存: userId={}, actionType={}", event.getUserId(), event.getActionType());
        } catch (Exception e) {
            log.error("处理通知事件失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响主业务流程
        }
    }
}
