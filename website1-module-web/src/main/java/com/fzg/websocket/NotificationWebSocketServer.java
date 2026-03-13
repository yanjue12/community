package com.fzg.websocket;

import com.alibaba.fastjson.JSON;
import com.fzg.service.UserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知WebSocket服务端点
 * 处理用户的WebSocket连接，用于实时推送通知消息
 * 增加连接验证和会话管理
 */
@ServerEndpoint("/ws/notification/{userId}")
@Component
@Slf4j
public class NotificationWebSocketServer {

    private static UserSessionService userSessionService;

    @Autowired
    public void setUserSessionService(UserSessionService userSessionService) {
        NotificationWebSocketServer.userSessionService = userSessionService;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdStr) {
        try {
            Long userId = Long.parseLong(userIdStr);
            
            // 从连接参数中获取token进行验证
            String token = getTokenFromSession(session);
            if (token == null || !validateUserToken(userId, token)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "connection");
                errorResponse.put("status", "error");
                errorResponse.put("message", "无权限建立WebSocket连接，请先登录");
                errorResponse.put("timestamp", System.currentTimeMillis());
                
                session.getBasicRemote().sendText(JSON.toJSONString(errorResponse));
                session.close();
                log.warn("用户{}无权限建立WebSocket连接", userId);
                return;
            }
            
            WebSocketManager.addUser(userId, session);
            
            // 发送连接成功消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "connection");
            response.put("status", "success");
            response.put("message", "WebSocket连接建立成功");
            response.put("timestamp", System.currentTimeMillis());
            
            session.getBasicRemote().sendText(JSON.toJSONString(response));
            log.info("用户{}的WebSocket连接建立成功", userId);
        } catch (Exception e) {
            log.error("WebSocket连接建立失败：{}", e.getMessage(), e);
            try {
                session.close();
            } catch (Exception closeException) {
                log.error("关闭异常连接失败：{}", closeException.getMessage());
            }
        }
    }

    /**
     * 从WebSocket会话中获取token
     */
    private String getTokenFromSession(Session session) {
        try {
            // 从查询参数中获取token
            String queryString = session.getQueryString();
            if (queryString != null && queryString.contains("token=")) {
                String[] params = queryString.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        return param.substring(6); // 去掉"token="前缀
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("获取WebSocket token失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证用户token
     */
    private boolean validateUserToken(Long userId, String token) {
        try {
            // 使用Sa-Token验证token
            Object loginId = cn.dev33.satoken.stp.StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                return false;
            }
            
            // 检查用户ID是否匹配
            return userId.equals(Long.valueOf(loginId.toString()));
        } catch (Exception e) {
            log.warn("验证用户{}的token失败: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("userId") String userIdStr) {
        try {
            Long userId = Long.parseLong(userIdStr);
            WebSocketManager.removeUser(userId, session);
            log.info("用户{}的WebSocket连接已关闭", userId);
        } catch (Exception e) {
            log.error("WebSocket连接关闭处理失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userIdStr) {
        try {
            Long userId = Long.parseLong(userIdStr);
            log.debug("收到用户{}的WebSocket消息：{}", userId, message);
            
            // 解析客户端消息
            Map<String, Object> clientMessage = JSON.parseObject(message, Map.class);
            String type = (String) clientMessage.get("type");
            
            // 处理心跳消息
            if ("heartbeat".equals(type)) {
                // 心跳时检查token是否还有效
                String token = getTokenFromSession(session);
                if (token == null || !validateUserToken(userId, token)) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("type", "force_disconnect");
                    errorResponse.put("reason", "登录已过期，请重新登录");
                    errorResponse.put("timestamp", System.currentTimeMillis());
                    
                    session.getBasicRemote().sendText(JSON.toJSONString(errorResponse));
                    session.close();
                    log.warn("用户{}登录已过期，断开WebSocket连接", userId);
                    return;
                }
                
                // 更新用户活跃时间
                WebSocketManager.updateUserActiveTime(userId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("type", "heartbeat");
                response.put("status", "ok");
                response.put("timestamp", System.currentTimeMillis());
                session.getBasicRemote().sendText(JSON.toJSONString(response));
                return;
            }
            
            // 处理其他类型的消息前也检查登录状态
            String token = getTokenFromSession(session);
            if (token == null || !validateUserToken(userId, token)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "force_disconnect");
                errorResponse.put("reason", "登录已过期，请重新登录");
                errorResponse.put("timestamp", System.currentTimeMillis());
                
                session.getBasicRemote().sendText(JSON.toJSONString(errorResponse));
                session.close();
                log.warn("用户{}登录已过期，断开WebSocket连接", userId);
                return;
            }
            
            // 处理其他类型的消息
            Map<String, Object> response = new HashMap<>();
            response.put("type", "message");
            response.put("status", "received");
            response.put("originalMessage", message);
            response.put("timestamp", System.currentTimeMillis());
            
            session.getBasicRemote().sendText(JSON.toJSONString(response));
        } catch (Exception e) {
            log.error("处理WebSocket消息失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error, @PathParam("userId") String userIdStr) {
        try {
            Long userId = Long.parseLong(userIdStr);
            log.error("用户{}的WebSocket连接发生错误：{}", userId, error.getMessage(), error);
            WebSocketManager.removeUser(userId, session);
        } catch (Exception e) {
            log.error("WebSocket错误处理失败：{}", e.getMessage(), e);
        }
    }
}