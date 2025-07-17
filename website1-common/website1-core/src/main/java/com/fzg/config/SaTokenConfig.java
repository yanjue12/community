package com.fzg.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 登录验证 -- 拦截所有路由，并排除公共接口
                    SaRouter.match("/**")
                            .notMatch("/app/**") // 这里配置公共接口路径
                            .match("/admin/**")
                            //.match("/minio/upload")
                            .check(StpUtil::checkLogin);
                }))
                .addPathPatterns("/**");
    }
}