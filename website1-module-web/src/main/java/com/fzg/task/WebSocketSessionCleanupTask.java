package com.fzg.task;

import com.fzg.websocket.WebSocketManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话清理定时任务
 * 定期检查用户登录状态，清理过期连接
 */
@Component
@Slf4j
public class WebSocketSessionCleanupTask {

    /**
     * 每5分钟检查一次用户登录状态
     * 清理登录过期的WebSocket连接
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    public void cleanupExpiredSessions() {
        try {
            log.debug("开始检查WebSocket连接的用户登录状态...");
            
            ConcurrentHashMap<Long, Session> onlineUsers = WebSocketManager.getOnlineUsers();
            int expiredCount = 0;
            
            for (Long userId : onlineUsers.keySet()) {
                Session session = onlineUsers.get(userId);
                if (session == null || !session.isOpen()) {
                    continue;
                }
                
                // 从session中获取token并验证
                String token = getTokenFromSession(session);
                if (token == null || !validateUserToken(userId, token)) {
                    // 用户登录已过期，强制断开连接
                    boolean disconnected = WebSocketManager.forceDisconnectUser(userId, "登录已过期，请重新登录");
                    if (disconnected) {
                        expiredCount++;
                        log.info("清理用户{}的过期WebSocket连接", userId);
                    }
                }
            }
            
            if (expiredCount > 0) {
                log.info("清理了{}个登录过期的WebSocket连接", expiredCount);
            }
            
        } catch (Exception e) {
            log.error("清理过期WebSocket连接时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 从WebSocket会话中获取token
     */
    private String getTokenFromSession(Session session) {
        try {
            String queryString = session.getQueryString();
            if (queryString != null && queryString.contains("token=")) {
                String[] params = queryString.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        return java.net.URLDecoder.decode(param.substring(6), "UTF-8");
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
            Object loginId = cn.dev33.satoken.stp.StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                return false;
            }
            return userId.equals(Long.valueOf(loginId.toString()));
        } catch (Exception e) {
            log.debug("验证用户{}的token失败: {}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 每小时统计一次在线用户信息
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1小时
    public void logOnlineUserStats() {
        try {
            int onlineCount = WebSocketManager.getOnlineUserCount();
            log.info("当前WebSocket在线用户数: {}", onlineCount);
        } catch (Exception e) {
            log.error("统计在线用户信息时发生错误: {}", e.getMessage(), e);
        }
    }
}