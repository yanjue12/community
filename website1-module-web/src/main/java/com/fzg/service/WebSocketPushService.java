package com.fzg.service;

import com.alibaba.fastjson.JSON;
import com.fzg.websocket.WebSocketManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket推送服务
 * 负责向在线用户推送实时通知
 */
@Service
@Slf4j
public class WebSocketPushService {

    /**
     * 推送通知给指定用户
     * 
     * @param userId 用户ID
     * @param type 通知类型
     * @param title 通知标题
     * @param content 通知内容
     * @param extraData 额外数据
     * @return 是否推送成功
     */
    public boolean pushNotificationToUser(Long userId, String type, String title, String content, Map<String, Object> extraData) {
        if (userId == null) {
            return false;
        }

        // 检查用户是否在线
        if (!WebSocketManager.isUserOnline(userId)) {
            log.debug("用户{}不在线，跳过WebSocket推送", userId);
            return false;
        }

        // 构建推送消息
        Map<String, Object> message = new HashMap<>();
        message.put("type", "notification");
        message.put("notificationType", type);
        message.put("title", title);
        message.put("content", content);
        message.put("timestamp", System.currentTimeMillis());
        
        if (extraData != null && !extraData.isEmpty()) {
            message.put("extraData", extraData);
        }

        // 发送消息
        String messageJson = JSON.toJSONString(message);
        boolean success = WebSocketManager.sendMessageToUser(userId, messageJson);
        
        if (success) {
            log.info("成功推送通知给用户{}：{}", userId, title);
        } else {
            log.warn("推送通知给用户{}失败：{}", userId, title);
        }
        
        return success;
    }

    /**
     * 推送系统通知给所有在线用户
     * 
     * @param title 通知标题
     * @param content 通知内容
     */
    public void pushSystemNotificationToAll(String title, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "system_notification");
        message.put("title", title);
        message.put("content", content);
        message.put("timestamp", System.currentTimeMillis());

        String messageJson = JSON.toJSONString(message);
        WebSocketManager.broadcastMessage(messageJson);
        
        log.info("推送系统通知给所有在线用户：{}", title);
    }

    /**
     * 推送未读消息数量更新
     * 
     * @param userId 用户ID
     * @param unreadCount 未读消息数量
     */
    public boolean pushUnreadCountUpdate(Long userId, int unreadCount) {
        if (userId == null) {
            return false;
        }

        if (!WebSocketManager.isUserOnline(userId)) {
            return false;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("type", "unread_count");
        message.put("count", unreadCount);
        message.put("timestamp", System.currentTimeMillis());

        String messageJson = JSON.toJSONString(message);
        boolean success = WebSocketManager.sendMessageToUser(userId, messageJson);
        
        if (success) {
            log.debug("成功推送未读消息数量给用户{}：{}", userId, unreadCount);
        }
        
        return success;
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return WebSocketManager.getOnlineUserCount();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        return WebSocketManager.isUserOnline(userId);
    }
}