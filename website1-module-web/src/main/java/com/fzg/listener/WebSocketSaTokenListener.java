package com.fzg.listener;

import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.SaLoginModel;
import com.fzg.websocket.WebSocketManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sa-Token事件监听器
 * 监听用户登录、登出、踢下线等事件，同步处理WebSocket连接
 */
@Component
@Slf4j
public class WebSocketSaTokenListener implements SaTokenListener {

    /**
     * 每次登录时触发
     */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginModel loginModel) {
        try {
            Long userId = Long.valueOf(loginId.toString());
            log.info("用户{}登录成功，token: {}", userId, tokenValue);
            // 登录时不需要特殊处理WebSocket，连接时会自动验证
        } catch (Exception e) {
            log.error("处理用户登录事件时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 每次注销时触发
     */
    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        try {
            Long userId = Long.valueOf(loginId.toString());
            log.info("=== Sa-Token监听器：用户{}登出 ===", userId);
            log.info("loginType: {}, tokenValue: {}", loginType, tokenValue);
            
            boolean disconnected = WebSocketManager.forceDisconnectUser(userId, "用户已登出");
            if (disconnected) {
                log.info("✅ 成功断开用户{}的WebSocket连接", userId);
            } else {
                log.info("⚠️ 用户{}没有WebSocket连接或断开失败", userId);
            }
        } catch (Exception e) {
            log.error("❌ 处理用户登出事件时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 每次被踢下线时触发
     */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        try {
            Long userId = Long.valueOf(loginId.toString());
            log.info("用户{}被踢下线，token: {}", userId, tokenValue);
            WebSocketManager.forceDisconnectUser(userId, "账号在其他地方登录");
        } catch (Exception e) {
            log.error("处理用户被踢下线事件时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 每次被顶下线时触发
     */
    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        try {
            Long userId = Long.valueOf(loginId.toString());
            log.info("用户{}被顶下线，token: {}", userId, tokenValue);
            WebSocketManager.forceDisconnectUser(userId, "账号在其他地方登录");
        } catch (Exception e) {
            log.error("处理用户被顶下线事件时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 每次被封禁时触发
     */
    @Override
    public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {
        try {
            Long userId = Long.valueOf(loginId.toString());
            log.info("用户{}被封禁，服务: {}, 等级: {}, 时长: {}秒", userId, service, level, disableTime);
            WebSocketManager.forceDisconnectUser(userId, "账号已被禁用");
        } catch (Exception e) {
            log.error("处理用户被封禁事件时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 每次被解封时触发
     */
    @Override
    public void doUntieDisable(String loginType, Object loginId, String service) {
        try {
            Long userId = Long.valueOf(loginId.toString());
            log.info("用户{}被解封，服务: {}", userId, service);
        } catch (Exception e) {
            log.error("处理用户被解封事件时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 每次打开二级认证时触发
     */
    @Override
    public void doOpenSafe(String loginType, String tokenValue, String service, long safeTime) {
        log.debug("打开二级认证，token: {}, 服务: {}, 时长: {}秒", tokenValue, service, safeTime);
    }

    /**
     * 每次关闭二级认证时触发
     */
    @Override
    public void doCloseSafe(String loginType, String tokenValue, String service) {
        log.debug("关闭二级认证，token: {}, 服务: {}", tokenValue, service);
    }

    /**
     * 每次创建Session时触发
     */
    @Override
    public void doCreateSession(String id) {
        log.debug("创建Session: {}", id);
    }

    /**
     * 每次注销Session时触发
     */
    @Override
    public void doLogoutSession(String id) {
        log.debug("注销Session: {}", id);
    }

    /**
     * 每次Token续期时触发
     */
    @Override
    public void doRenewTimeout(String tokenValue, Object loginId, long timeout) {
        log.debug("Token续期，token: {}, 用户: {}, 超时时间: {}秒", tokenValue, loginId, timeout);
    }
}