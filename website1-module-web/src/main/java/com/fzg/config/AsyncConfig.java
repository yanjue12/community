package com.fzg.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步处理配置
 * 为通知系统提供独立的线程池，避免与其他业务竞争资源
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * 通知处理线程池
     * 核心线程数: 10
     * 最大线程数: 50
     * 队列容量: 1000
     * 线程名前缀: notification-
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        log.info("通知处理线程池已初始化: corePoolSize=10, maxPoolSize=50, queueCapacity=1000");
        return executor;
    }

    /**
     * 批量操作线程池
     * 用于处理批量通知操作（如新文章推送给所有粉丝）
     */
    @Bean(name = "batchNotificationExecutor")
    public Executor batchNotificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("batch-notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        log.info("批量通知处理线程池已初始化: corePoolSize=5, maxPoolSize=20, queueCapacity=500");
        return executor;
    }
}
