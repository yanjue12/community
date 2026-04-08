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
     * 向指定用户推送通知
     *
     * @param userId     目标用户ID
     * @param actionType 动作类型
     * @param title      通知标题
     * @param content    通知内容
     * @param extraData  额外数据
     * @return 是否推送成功
     */
    public boolean pushNotificationToUser(Long userId, String actionType, String title, String content, Map<String, Object> extraData) {
        try {
            String eventId = extraData != null ? (String) extraData.get("eventId") : "unknown";
            log.info("=== 开始推送通知 [{}] ===", eventId);
            log.info("目标用户ID: {}, 动作类型: {}, 标题: {}", userId, actionType, title);
            
            // 检查用户是否在线
            boolean isOnline = WebSocketManager.isUserOnline(userId);
            log.info("检查用户{}在线状态: {}", userId, isOnline ? "在线" : "离线");
            
            if (!isOnline) {
                log.info("用户{}不在线，跳过WebSocket推送 [{}]", userId, eventId);
                return false;
            }

            // 构建通知消息
            Map<String, Object> message = buildNotificationMessage(actionType, title, content, extraData);

            // 发送WebSocket消息
            String messageJson = JSON.toJSONString(message);
            log.info("准备发送WebSocket消息 [{}]: {}", eventId, messageJson);
            
            boolean success = WebSocketManager.sendMessageToUser(userId, messageJson);

            if (success) {
                log.info("向用户{}推送通知成功 [{}]: {}", userId, eventId, title);
            } else {
                log.warn("向用户{}推送通知失败 [{}]: {}", userId, eventId, title);
            }

            return success;
        } catch (Exception e) {
            log.error("推送通知给用户{}时发生错误: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 构建通知消息格式
     */
    private Map<String, Object> buildNotificationMessage(String actionType, String title, String content, Map<String, Object> extraData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "notification");
        message.put("timestamp", System.currentTimeMillis());

        // 构建通知数据 - 包装在 data 字段中
        Map<String, Object> data = new HashMap<>();
        data.put("notificationType", mapActionTypeToNotificationType(actionType));
        data.put("title", title);
        data.put("content", content);

        // 处理额外数据
        if (extraData != null) {
            // 提取相关ID
            if (extraData.containsKey("articleId")) {
                data.put("relatedId", extraData.get("articleId"));
            } else if (extraData.containsKey("commentId")) {
                data.put("relatedId", extraData.get("commentId"));
            } else if (extraData.containsKey("targetId")) {
                data.put("relatedId", extraData.get("targetId"));
            }

            // 构建发送者信息
            Map<String, Object> fromUser = new HashMap<>();
            fromUser.put("id", extraData.getOrDefault("fromUserId", 0));
            fromUser.put("nickname", extraData.getOrDefault("commenterName", "系统"));
            fromUser.put("avatar", extraData.getOrDefault("fromUserAvatar", ""));
            data.put("fromUser", fromUser);
        }

        // 将数据包装在 data 字段中
        message.put("data", data);
        return message;
    }

    /**
     * 将后端动作类型映射为前端通知类型
     */
    private String mapActionTypeToNotificationType(String actionType) {
        if (actionType == null) {
            return "SYSTEM";
        }

        switch (actionType.toUpperCase()) {
            case "LIKE":
            case "ARTICLE_LIKE":
                return "LIKE";
            case "COMMENT":
            case "COMMENT_ARTICLE":  // 添加这个映射
            case "ARTICLE_COMMENT":
                return "COMMENT";
            case "REPLY":
            case "COMMENT_REPLY":
                return "REPLY";
            case "FOLLOW":
            case "USER_FOLLOW":
                return "FOLLOW";
            case "MENTION":
            case "USER_MENTION":
                return "MENTION";
            case "FAVORITE":
            case "ARTICLE_FAVORITE":
                return "FAVORITE";
            case "SHARE":
            case "ARTICLE_SHARE":
                return "SHARE";
            case "COMMENT_LIKE":
                return "COMMENT_LIKE";
            default:
                log.warn("未知的动作类型: {}", actionType);
                return "SYSTEM";
        }
    }

    /**
     * 推送私信消息给接收方（如果在线）
     *
     * @param message 私信消息对象
     * @param senderNickname 发送者昵称
     * @param senderAvatar   发送者头像
     */
    public boolean pushPrivateMessage(com.fzg.model.PrivateMessage message,
                                      String senderNickname, String senderAvatar) {
        try {
            if (!WebSocketManager.isUserOnline(message.getReceiverId())) {
                log.debug("用户{}不在线，私信不推送WebSocket", message.getReceiverId());
                return false;
            }

            Map<String, Object> sender = new HashMap<>();
            sender.put("id", message.getSenderId());
            sender.put("nickname", senderNickname);
            sender.put("avatar", senderAvatar);

            Map<String, Object> data = new HashMap<>();
            data.put("messageId", message.getId());
            data.put("conversationId", message.getConversationId());
            data.put("content", message.getContent());
            data.put("contentType", message.getContentType());
            data.put("createdAt", message.getCreatedAt());
            data.put("sender", sender);

            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "private_message");
            payload.put("data", data);
            payload.put("timestamp", System.currentTimeMillis());

            String json = com.alibaba.fastjson.JSON.toJSONString(payload);
            boolean success = WebSocketManager.sendMessageToUser(message.getReceiverId(), json);
            log.info("私信WebSocket推送 -> 用户{} {}", message.getReceiverId(), success ? "成功" : "失败");
            return success;
        } catch (Exception e) {
            log.error("推送私信失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcastMessage(String title, String content) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "broadcast");
            message.put("title", title);
            message.put("content", content);
            message.put("timestamp", System.currentTimeMillis());

            String messageJson = JSON.toJSONString(message);
            WebSocketManager.broadcastMessage(messageJson);
            log.info("广播消息成功: {}", title);
        } catch (Exception e) {
            log.error("广播消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送系统通知给所有在线用户
     * 
     * @param title 通知标题
     * @param content 通知内容
     */
    public void pushSystemNotificationToAll(String title, String content) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "system_notification");
            message.put("title", title);
            message.put("content", content);
            message.put("timestamp", System.currentTimeMillis());

            String messageJson = JSON.toJSONString(message);
            WebSocketManager.broadcastMessage(messageJson);
            log.info("推送系统通知给所有在线用户：{}", title);
        } catch (Exception e) {
            log.error("推送系统通知失败: {}", e.getMessage(), e);
        }
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
