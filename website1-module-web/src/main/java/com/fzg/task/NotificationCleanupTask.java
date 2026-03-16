package com.fzg.task;

import com.fzg.mapper.Notificationmapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 通知清理定时任务
 * 每天凌晨3点执行，物理删除15天前已读的通知记录
 * 使用雪花算法ID，支持分布式环境
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationCleanupTask {

    private final Notificationmapper notificationMapper;
    
    /**
     * 物理删除半个月前的已读通知
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupReadNotifications() {
        try {
            log.info("=== 开始执行通知清理任务 ===");
            
            // 计算半个月前的时间（15天）
            LocalDateTime halfMonthAgo = LocalDateTime.now().minusDays(15);
            String cutoffTime = halfMonthAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            log.info("清理时间节点: {}", cutoffTime);
            
            // 执行物理删除
            int deletedCount = notificationMapper.physicalDeleteReadNotifications(cutoffTime);
            
            if (deletedCount > 0) {
                log.info("通知清理任务完成，共删除 {} 条半个月前的已读通知记录", deletedCount);
            } else {
                log.info("通知清理任务完成，没有需要清理的记录");
            }
            
        } catch (Exception e) {
            log.error("通知清理任务执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 清理逻辑删除的通知（可选）
     * 每周日凌晨4点执行，清理30天前逻辑删除的记录
     */
    @Scheduled(cron = "0 0 4 * * SUN")
    public void cleanupLogicalDeletedNotifications() {
        try {
            log.info("=== 开始执行逻辑删除通知清理任务 ===");
            
            // 计算30天前的时间
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            String cutoffTime = thirtyDaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            log.info("清理时间节点: {}", cutoffTime);
            
            // 执行物理删除逻辑删除的记录
            int deletedCount = notificationMapper.physicalDeleteLogicalDeletedNotifications(cutoffTime);
            
            if (deletedCount > 0) {
                log.info("逻辑删除通知清理任务完成，共删除 {} 条记录", deletedCount);
            } else {
                log.info("逻辑删除通知清理任务完成，没有需要清理的记录");
            }
            
        } catch (Exception e) {
            log.error("逻辑删除通知清理任务执行失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 统计通知数据（可选）
     * 每天凌晨5点执行，输出通知统计信息
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void notificationStatistics() {
        try {
            log.info("=== 开始统计通知数据 ===");
            
            // 统计总通知数
            Long totalCount = notificationMapper.countTotalNotifications();
            
            // 统计未读通知数
            Long unreadCount = notificationMapper.countUnreadNotifications();
            
            // 统计已读通知数
            Long readCount = notificationMapper.countReadNotifications();
            
            // 统计逻辑删除通知数
            Long deletedCount = notificationMapper.countLogicalDeletedNotifications();
            
            log.info("通知统计数据:");
            log.info("   总通知数: {}", totalCount);
            log.info("   未读通知数: {}", unreadCount);
            log.info("   已读通知数: {}", readCount);
            log.info("   逻辑删除通知数: {}", deletedCount);
            
        } catch (Exception e) {
            log.error("通知统计任务执行失败: {}", e.getMessage(), e);
        }
    }
}