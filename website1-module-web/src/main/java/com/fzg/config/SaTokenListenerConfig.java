package com.fzg.config;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.listener.SaTokenListener;
import com.fzg.listener.WebSocketSaTokenListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Sa-Token监听器配置
 * 注册WebSocket相关的Sa-Token事件监听器
 */
@Configuration
public class SaTokenListenerConfig {

    /**
     * 注册Sa-Token监听器
     * 监听用户登录状态变化，同步处理WebSocket连接
     */
    @Bean
    @Primary
    public SaTokenListener saTokenListener() {
        return new WebSocketSaTokenListener();
    }
}