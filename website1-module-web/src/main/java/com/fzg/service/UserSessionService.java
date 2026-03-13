package com.fzg.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户会话管理服务
 * 处理用户登录状态验证
 */
@Service
@Slf4j
public class UserSessionService {

    /**
     * 检查用户是否有权限建立WebSocket连接
     * 集成Sa-Token登录验证逻辑
     */
    public boolean canUserConnectWebSocket(Long userId) {
        if (userId == null) {
            return false;
        }

        try {
            // 使用Sa-Token检查用户登录状态
            cn.dev33.satoken.stp.StpUtil.checkLogin();
            
            // 检查当前登录用户ID是否匹配
            Object loginId = cn.dev33.satoken.stp.StpUtil.getLoginId();
            if (!userId.equals(Long.valueOf(loginId.toString()))) {
                log.warn("WebSocket连接用户ID{}与登录用户ID{}不匹配", userId, loginId);
                return false;
            }
            
            // 检查用户是否被禁用
            // User user = userService.getById(userId);
            // if (user == null || "1".equals(user.getStatus())) {
            //     log.warn("用户{}不存在或已被禁用", userId);
            //     return false;
            // }
            
            return true;
        } catch (cn.dev33.satoken.exception.NotLoginException e) {
            log.warn("用户{}未登录或登录已过期，无法建立WebSocket连接: {}", userId, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("检查用户{}WebSocket连接权限时发生错误: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取用户连接统计信息
     */
    public String getUserConnectionInfo(Long userId) {
        if (userId == null) {
            return "用户ID为空";
        }

        boolean isOnline = com.fzg.websocket.WebSocketManager.isUserOnline(userId);
        long duration = com.fzg.websocket.WebSocketManager.getUserConnectionDuration(userId);
        
        if (isOnline) {
            long hours = duration / (1000 * 60 * 60);
            long minutes = (duration % (1000 * 60 * 60)) / (1000 * 60);
            return String.format("用户在线，连接时长：%d小时%d分钟", hours, minutes);
        } else {
            return "用户离线";
        }
    }
}