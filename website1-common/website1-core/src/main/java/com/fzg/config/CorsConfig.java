package com.fzg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类，用于配置允许跨域的规则
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 创建 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();
        // 允许任何域名使用
        config.addAllowedOriginPattern("*");
        // 允许任何请求头
        config.addAllowedHeader("*");
        // 允许任何方法（POST、GET 等）
        config.addAllowedMethod("*");
        // 允许携带凭证（如 Cookie）
        config.setAllowCredentials(true);

        // 创建 CORS 配置源，将配置应用到所有请求路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 创建并返回 CorsFilter 实例
        return new CorsFilter(source);
    }
}