package com.fzg.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket连接管理器
 * 管理用户的WebSocket连接，支持在线用户检测和消息推送
 * 增加连接验证和定期清理机制
 */
@Component
@Slf4j
public class WebSocketManager {

    /**
     * 存储用户ID与WebSocket会话的映射关系
     * key: userId, value: WebSocket Session
     */
    private static final ConcurrentHashMap<Long, Session> USER_SESSIONS = new ConcurrentHashMap<>();

    /**
     * 存储用户连接时间
     * key: userId, value: 连接时间戳
     */
    private static final ConcurrentHashMap<Long, Long> USER_CONNECT_TIME = new ConcurrentHashMap<>();

    /**
     * 存储所有在线的WebSocket连接
     */
    private static final CopyOnWriteArraySet<Session> ONLINE_SESSIONS = new CopyOnWriteArraySet<>();

    /**
     * 定时任务执行器，用于清理无效连接
     */
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    /**
     * 连接最大空闲时间（毫秒）- 2小时
     */
    private static final long MAX_IDLE_TIME = 2 * 60 * 60 * 1000L;

    /**
     * 心跳检测间隔（毫秒）- 5分钟
     */
    private static final long HEARTBEAT_INTERVAL = 5 * 60 * 1000L;

    static {
        // 启动定时清理任务
        SCHEDULER.scheduleAtFixedRate(WebSocketManager::cleanupInvalidConnections, 
                HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * 用户连接时调用
     */
    public static void addUser(Long userId, Session session) {
        if (userId != null && session != null) {
            // 如果用户已有连接，先关闭旧连接
            Session oldSession = USER_SESSIONS.get(userId);
            if (oldSession != null && oldSession.isOpen()) {
                try {
                    oldSession.close();
                    log.info("关闭用户{}的旧WebSocket连接", userId);
                } catch (IOException e) {
                    log.warn("关闭用户{}旧连接失败: {}", userId, e.getMessage());
                }
            }

            USER_SESSIONS.put(userId, session);
            USER_CONNECT_TIME.put(userId, System.currentTimeMillis());
            ONLINE_SESSIONS.add(session);
            log.info("用户{}建立WebSocket连接，当前在线用户数：{}", userId, USER_SESSIONS.size());
        }
    }

    /**
     * 用户断开连接时调用
     */
    public static void removeUser(Long userId, Session session) {
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            USER_CONNECT_TIME.remove(userId);
            log.info("用户{}断开WebSocket连接，当前在线用户数：{}", userId, USER_SESSIONS.size());
        }
        if (session != null) {
            ONLINE_SESSIONS.remove(session);
        }
    }

    /**
     * 更新用户活跃时间（收到心跳时调用）
     */
    public static void updateUserActiveTime(Long userId) {
        if (userId != null && USER_SESSIONS.containsKey(userId)) {
            USER_CONNECT_TIME.put(userId, System.currentTimeMillis());
            log.debug("更新用户{}活跃时间", userId);
        }
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isUserOnline(Long userId) {
        if (userId == null) {
            return false;
        }
        Session session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 向指定用户发送消息
     */
    public static boolean sendMessageToUser(Long userId, String message) {
        if (userId == null || message == null) {
            return false;
        }

        Session session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.getBasicRemote().sendText(message);
                }
                log.debug("向用户{}发送WebSocket消息成功：{}", userId, message);
                return true;
            } catch (IOException e) {
                log.error("向用户{}发送WebSocket消息失败：{}", userId, e.getMessage());
                // 发送失败时移除无效连接
                removeUser(userId, session);
                return false;
            }
        }
        return false;
    }

    /**
     * 广播消息给所有在线用户
     */
    public static void broadcastMessage(String message) {
        if (message == null) {
            return;
        }

        for (Session session : ONLINE_SESSIONS) {
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        session.getBasicRemote().sendText(message);
                    }
                } catch (IOException e) {
                    log.error("广播消息失败：{}", e.getMessage());
                    ONLINE_SESSIONS.remove(session);
                }
            } else {
                ONLINE_SESSIONS.remove(session);
            }
        }
        log.debug("广播消息给{}个在线用户：{}", ONLINE_SESSIONS.size(), message);
    }

    /**
     * 定期清理无效连接
     */
    private static void cleanupInvalidConnections() {
        try {
            long currentTime = System.currentTimeMillis();
            int cleanedCount = 0;

            // 清理超时连接
            for (Long userId : USER_CONNECT_TIME.keySet()) {
                Long connectTime = USER_CONNECT_TIME.get(userId);
                if (connectTime != null && (currentTime - connectTime) > MAX_IDLE_TIME) {
                    Session session = USER_SESSIONS.get(userId);
                    if (session != null) {
                        try {
                            session.close();
                        } catch (IOException e) {
                            log.warn("关闭超时连接失败: {}", e.getMessage());
                        }
                    }
                    removeUser(userId, session);
                    cleanedCount++;
                    log.info("清理用户{}的超时WebSocket连接", userId);
                }
            }

            // 清理已关闭的连接
            for (Long userId : USER_SESSIONS.keySet()) {
                Session session = USER_SESSIONS.get(userId);
                if (session == null || !session.isOpen()) {
                    removeUser(userId, session);
                    cleanedCount++;
                    log.info("清理用户{}的无效WebSocket连接", userId);
                }
            }

            if (cleanedCount > 0) {
                log.info("清理了{}个无效WebSocket连接，当前在线用户数：{}", cleanedCount, USER_SESSIONS.size());
            }
        } catch (Exception e) {
            log.error("清理无效连接时发生错误：{}", e.getMessage(), e);
        }
    }

    /**
     * 强制断开指定用户的连接（用于登录过期等场景）
     */
    public static boolean forceDisconnectUser(Long userId, String reason) {
        if (userId == null) {
            log.warn("forceDisconnectUser: 用户ID为空");
            return false;
        }

        log.info("尝试强制断开用户{}的WebSocket连接，原因：{}", userId, reason);
        
        Session session = USER_SESSIONS.get(userId);
        if (session == null) {
            log.info("用户{}没有WebSocket连接", userId);
            return false;
        }
        
        if (!session.isOpen()) {
            log.info("用户{}的WebSocket连接已关闭", userId);
            removeUser(userId, session);
            return false;
        }
        
        try {
            // 发送断开连接通知
            String disconnectMessage = String.format(
                "{\"type\":\"force_disconnect\",\"reason\":\"%s\",\"timestamp\":%d}",
                reason, System.currentTimeMillis()
            );
            session.getBasicRemote().sendText(disconnectMessage);
            log.info("已向用户{}发送断开连接通知", userId);
            
            // 关闭连接
            session.close();
            removeUser(userId, session);
            log.info("✅ 成功强制断开用户{}的WebSocket连接", userId);
            return true;
        } catch (IOException e) {
            log.error("❌ 强制断开用户{}连接失败：{}", userId, e.getMessage());
            removeUser(userId, session);
            return false;
        }
    }

    /**
     * 获取在线用户数量
     */
    public static int getOnlineUserCount() {
        return USER_SESSIONS.size();
    }

    /**
     * 获取所有在线用户ID
     */
    public static ConcurrentHashMap<Long, Session> getOnlineUsers() {
        return new ConcurrentHashMap<>(USER_SESSIONS);
    }

    /**
     * 获取用户连接时长（毫秒）
     */
    public static long getUserConnectionDuration(Long userId) {
        if (userId == null) {
            return 0;
        }
        Long connectTime = USER_CONNECT_TIME.get(userId);
        if (connectTime != null) {
            return System.currentTimeMillis() - connectTime;
        }
        return 0;
    }

    /**
     * 关闭所有连接（应用关闭时调用）
     */
    public static void shutdown() {
        log.info("开始关闭所有WebSocket连接...");
        
        for (Session session : ONLINE_SESSIONS) {
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (IOException e) {
                log.warn("关闭WebSocket连接失败：{}", e.getMessage());
            }
        }
        
        USER_SESSIONS.clear();
        USER_CONNECT_TIME.clear();
        ONLINE_SESSIONS.clear();
        SCHEDULER.shutdown();
        
        log.info("所有WebSocket连接已关闭");
    }
}