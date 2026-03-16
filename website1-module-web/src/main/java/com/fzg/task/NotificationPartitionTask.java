package com.fzg.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 通知表分区管理任务
 * 功能：
 * 1. 自动创建未来分区
 * 2. 清理过期分区（物理删除半个月前的已读通知）
 * 3. 维护分区表结构
 */
@Component
@Slf4j
public class NotificationPartitionTask {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TABLE_NAME = "notification";
    private static final DateTimeFormatter PARTITION_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * 每天凌晨3点执行分区维护任务
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void maintainPartitions() {
        log.info("=== 开始执行通知表分区维护任务 ===");
        
        try {
            // 1. 检查表是否已分区，如果没有则初始化分区
            initializePartitionIfNeeded();
            
            // 2. 创建未来7天的分区
            createFuturePartitions();
            
            // 3. 清理过期分区（半个月前的已读通知）
            cleanupExpiredPartitions();
            
            // 4. 优化分区表
            optimizePartitions();
            
            log.info("=== 通知表分区维护任务执行完成 ===");
            
        } catch (Exception e) {
            log.error("通知表分区维护任务执行失败", e);
            throw e;
        }
    }

    /**
     * 初始化分区表（如果表还没有分区）
     */
    private void initializePartitionIfNeeded() {
        try {
            // 检查表是否已经分区
            String checkPartitionSql =
                    "SELECT COUNT(*) as partition_count " +
                            "FROM INFORMATION_SCHEMA.PARTITIONS " +
                            "WHERE TABLE_SCHEMA = DATABASE() " +
                            "AND TABLE_NAME = ? " +
                            "AND PARTITION_NAME IS NOT NULL";
            
            Integer partitionCount = jdbcTemplate.queryForObject(
                checkPartitionSql, 
                Integer.class, 
                TABLE_NAME
            );
            
            if (partitionCount == null || partitionCount == 0) {
                log.info("表 {} 尚未分区，开始初始化分区...", TABLE_NAME);
                initializePartitions();
            } else {
                log.info("表 {} 已存在 {} 个分区", TABLE_NAME, partitionCount);
            }
            
        } catch (Exception e) {
            log.error("检查分区状态失败", e);
            throw e;
        }
    }

    /**
     * 初始化分区表结构
     */
    private void initializePartitions() {
        try {
            // 创建分区表（按日期范围分区）
            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(30); // 从30天前开始
            LocalDate endDate = today.plusDays(30);    // 到30天后结束
            
            StringBuilder sql = new StringBuilder();
            sql.append("ALTER TABLE ").append(TABLE_NAME).append(" ");
            sql.append("PARTITION BY RANGE (TO_DAYS(created_at)) (");
            
            // 创建历史分区（按周分区）
            LocalDate current = startDate;
            boolean first = true;
            
            while (current.isBefore(endDate)) {
                if (!first) {
                    sql.append(", ");
                }
                
                LocalDate weekEnd = current.plusDays(7);
                String partitionName = "p" + current.format(PARTITION_DATE_FORMAT);
                
                sql.append("PARTITION ").append(partitionName)
                   .append(" VALUES LESS THAN (TO_DAYS('")
                   .append(weekEnd).append("'))");
                
                current = weekEnd;
                first = false;
            }
            
            // 添加未来分区
            sql.append(", PARTITION p_future VALUES LESS THAN MAXVALUE");
            sql.append(")");
            
            log.info("执行分区初始化SQL: {}", sql.toString());
            jdbcTemplate.execute(sql.toString());
            
            log.info("表 {} 分区初始化完成", TABLE_NAME);
            
        } catch (Exception e) {
            log.error("初始化分区失败", e);
            throw e;
        }
    }

    /**
     * 创建未来分区
     */
    private void createFuturePartitions() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate futureDate = today.plusDays(7);
            
            // 检查是否需要创建新分区
            String partitionName = "p" + futureDate.format(PARTITION_DATE_FORMAT);

            String checkSql =
                    "SELECT COUNT(*) " +
                            "FROM INFORMATION_SCHEMA.PARTITIONS " +
                            "WHERE TABLE_SCHEMA = DATABASE() " +
                            "AND TABLE_NAME = ? " +
                            "AND PARTITION_NAME = ?";
            
            Integer exists = jdbcTemplate.queryForObject(
                checkSql, 
                Integer.class, 
                TABLE_NAME, 
                partitionName
            );
            
            if (exists == null || exists == 0) {
                // 重组p_future分区，添加新的日期分区
                LocalDate partitionEnd = futureDate.plusDays(7);
                // 重组分区的 SQL (String.format 依然可用)
                String reorganizeSql = String.format(
                        "ALTER TABLE %s " +
                                "REORGANIZE PARTITION p_future INTO (" +
                                "    PARTITION %s VALUES LESS THAN (TO_DAYS('%s')), " +
                                "    PARTITION p_future VALUES LESS THAN MAXVALUE" +
                                ")",
                        TABLE_NAME,
                        partitionName,
                        partitionEnd
                );
                
                log.info("创建未来分区: {}", partitionName);
                jdbcTemplate.execute(reorganizeSql);
                log.info("分区 {} 创建成功", partitionName);
            }
            
        } catch (Exception e) {
            log.error("创建未来分区失败", e);
            throw e;
        }
    }

    /**
     * 清理过期分区（物理删除半个月前的已读通知）
     */
    private void cleanupExpiredPartitions() {
        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(15);
            
            // 获取需要清理的分区列表
            String getPartitionsSql =
                    "SELECT PARTITION_NAME, PARTITION_DESCRIPTION " +
                            "FROM INFORMATION_SCHEMA.PARTITIONS " +
                            "WHERE TABLE_SCHEMA = DATABASE() " +
                            "AND TABLE_NAME = ? " +
                            "AND PARTITION_NAME IS NOT NULL " +
                            "AND PARTITION_NAME != 'p_future' " +
                            "ORDER BY PARTITION_NAME";
            
            List<Map<String, Object>> partitions = jdbcTemplate.queryForList(
                getPartitionsSql, 
                TABLE_NAME
            );
            
            for (Map<String, Object> partition : partitions) {
                String partitionName = (String) partition.get("PARTITION_NAME");
                String description = (String) partition.get("PARTITION_DESCRIPTION");
                
                try {
                    // 解析分区的截止日期
                    if (description != null && description.contains("TO_DAYS")) {
                        // 提取日期字符串
                        String dateStr = description.replaceAll(".*TO_DAYS\\('([^']+)'\\).*", "$1");
                        LocalDate partitionDate = LocalDate.parse(dateStr);
                        
                        if (partitionDate.isBefore(cutoffDate)) {
                            // 在删除分区前，先清理已读通知
                            cleanReadNotificationsInPartition(partitionName);
                            
                            // 检查分区是否为空，如果为空则删除
                            if (isPartitionEmpty(partitionName)) {
                                dropPartition(partitionName);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("处理分区 {} 时出错: {}", partitionName, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("清理过期分区失败", e);
            throw e;
        }
    }

    /**
     * 清理指定分区中的已读通知
     */
    private void cleanReadNotificationsInPartition(String partitionName) {
        try {
            String deleteSql = String.format(
                    "DELETE FROM %s PARTITION (%s) " +
                            "WHERE is_read = '1' " +
                            "AND created_at < DATE_SUB(NOW(), INTERVAL 15 DAY)",
                    TABLE_NAME,
                    partitionName
            );
            
            int deletedCount = jdbcTemplate.update(deleteSql);
            
            if (deletedCount > 0) {
                log.info("从分区 {} 中删除了 {} 条已读通知", partitionName, deletedCount);
            }
            
        } catch (Exception e) {
            log.error("清理分区 {} 中的已读通知失败", partitionName, e);
        }
    }

    /**
     * 检查分区是否为空
     */
    private boolean isPartitionEmpty(String partitionName) {
        try {
            String countSql = String.format(
                    "SELECT COUNT(*) FROM %s PARTITION (%s)",
                    TABLE_NAME,
                    partitionName
            );
            
            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);
            return count == null || count == 0;
            
        } catch (Exception e) {
            log.error("检查分区 {} 是否为空时出错", partitionName, e);
            return false;
        }
    }

    /**
     * 删除空分区
     */
    private void dropPartition(String partitionName) {
        try {
            String dropSql = String.format(
                    "ALTER TABLE %s DROP PARTITION %s",
                    TABLE_NAME,
                    partitionName
            );
            
            jdbcTemplate.execute(dropSql);
            log.info("已删除空分区: {}", partitionName);
            
        } catch (Exception e) {
            log.error("删除分区 {} 失败", partitionName, e);
            throw e;
        }
    }

    /**
     * 优化分区表
     */
    private void optimizePartitions() {
        try {
            // 获取所有分区
            String getPartitionsSql =
                    "SELECT PARTITION_NAME " +
                            "FROM INFORMATION_SCHEMA.PARTITIONS " +
                            "WHERE TABLE_SCHEMA = DATABASE() " +
                            "AND TABLE_NAME = ? " +
                            "AND PARTITION_NAME IS NOT NULL";
            
            List<String> partitions = jdbcTemplate.queryForList(
                getPartitionsSql, 
                String.class, 
                TABLE_NAME
            );
            
            // 优化每个分区
            for (String partitionName : partitions) {
                try {
                    String optimizeSql = String.format(
                            "ALTER TABLE %s OPTIMIZE PARTITION %s",
                            TABLE_NAME,
                            partitionName
                    );
                    
                    jdbcTemplate.execute(optimizeSql);
                    log.debug("分区 {} 优化完成", partitionName);
                    
                } catch (Exception e) {
                    log.warn("优化分区 {} 时出错: {}", partitionName, e.getMessage());
                }
            }
            
            log.info("分区表优化完成");
            
        } catch (Exception e) {
            log.error("优化分区表失败", e);
        }
    }

    /**
     * 手动触发分区维护（用于测试）
     */
    public void manualMaintenance() {
        log.info("手动触发分区维护任务");
        maintainPartitions();
    }

    /**
     * 获取分区信息
     */
    public List<Map<String, Object>> getPartitionInfo() {
        String sql =
                "SELECT " +
                        "    PARTITION_NAME, " +
                        "    PARTITION_DESCRIPTION, " +
                        "    TABLE_ROWS, " +
                        "    AVG_ROW_LENGTH, " +
                        "    DATA_LENGTH, " +
                        "    CREATE_TIME " +
                        "FROM INFORMATION_SCHEMA.PARTITIONS " +
                        "WHERE TABLE_SCHEMA = DATABASE() " +
                        "AND TABLE_NAME = ? " +
                        "AND PARTITION_NAME IS NOT NULL " +
                        "ORDER BY PARTITION_NAME";

        return jdbcTemplate.queryForList(sql, TABLE_NAME);
    }
}
