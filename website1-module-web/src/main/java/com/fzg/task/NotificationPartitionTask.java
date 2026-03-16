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
    public void dropPartition(String partitionName) {
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

    /**
     * 创建指定年月的分区
     * @param year 年份
     * @param month 月份
     */
    public void createPartitionForMonth(int year, int month) {
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.plusMonths(1);
            
            String partitionName = "p" + startDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
            
            // 检查分区是否已存在
            String checkSql =
                    "SELECT COUNT(*) " +
                            "FROM INFORMATION_SCHEMA.PARTITIONS " +
                            "WHERE TABLE_SCHEMA = DATABASE() " +
                            "AND TABLE_NAME = ? " +
                            "AND PARTITION_NAME = ?";
            
            Integer exists = jdbcTemplate.queryForObject(checkSql, Integer.class, TABLE_NAME, partitionName);
            
            if (exists != null && exists > 0) {
                log.info("分区 {} 已存在，跳过创建", partitionName);
                return;
            }
            
            // 创建分区 - 重组p_future分区
            String reorganizeSql = String.format(
                    "ALTER TABLE %s " +
                            "REORGANIZE PARTITION p_future INTO (" +
                            "    PARTITION %s VALUES LESS THAN (TO_DAYS('%s')), " +
                            "    PARTITION p_future VALUES LESS THAN MAXVALUE" +
                            ")",
                    TABLE_NAME,
                    partitionName,
                    endDate
            );
            
            log.info("创建分区: {} ({}年{}月)", partitionName, year, month);
            jdbcTemplate.execute(reorganizeSql);
            log.info("分区 {} 创建成功", partitionName);
            
        } catch (Exception e) {
            log.error("创建分区失败: {}年{}月", year, month, e);
            throw new RuntimeException("创建分区失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建下个月的分区
     */
    public void createNextMonthPartition() {
        try {
            LocalDate nextMonth = LocalDate.now().plusMonths(1);
            int year = nextMonth.getYear();
            int month = nextMonth.getMonthValue();
            
            log.info("开始创建下个月分区: {}年{}月", year, month);
            createPartitionForMonth(year, month);
            log.info("下个月分区创建完成");
            
        } catch (Exception e) {
            log.error("创建下个月分区失败", e);
            throw new RuntimeException("创建下个月分区失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除历史分区
     */
    public void dropOldPartitions() {
        try {
            log.info("开始删除历史分区");
            cleanupExpiredPartitions();
            log.info("历史分区删除完成");
            
        } catch (Exception e) {
            log.error("删除历史分区失败", e);
            throw new RuntimeException("删除历史分区失败: " + e.getMessage(), e);
        }
    }

    /**
     * 监控分区状态
     */
    public void monitorPartitionStatus() {
        try {
            log.info("=== 通知表分区状态监控 ===");
            
            List<Map<String, Object>> partitions = getPartitionInfo();
            
            if (partitions.isEmpty()) {
                log.warn("未找到任何分区信息");
                return;
            }
            
            log.info("当前分区总数: {}", partitions.size());
            log.info("分区详细信息:");
            log.info("{:<15} {:<20} {:<10} {:<15} {:<15} {:<20}", 
                    "分区名", "分区描述", "行数", "平均行长度", "数据长度", "创建时间");
            log.info("{}","-".repeat(100));
            
            long totalRows = 0;
            long totalDataLength = 0;
            
            for (Map<String, Object> partition : partitions) {
                String partitionName = (String) partition.get("PARTITION_NAME");
                String description = (String) partition.get("PARTITION_DESCRIPTION");
                Long tableRows = (Long) partition.get("TABLE_ROWS");
                Long avgRowLength = (Long) partition.get("AVG_ROW_LENGTH");
                Long dataLength = (Long) partition.get("DATA_LENGTH");
                Object createTime = partition.get("CREATE_TIME");
                
                // 处理null值
                tableRows = tableRows != null ? tableRows : 0L;
                avgRowLength = avgRowLength != null ? avgRowLength : 0L;
                dataLength = dataLength != null ? dataLength : 0L;
                
                totalRows += tableRows;
                totalDataLength += dataLength;
                
                // 简化描述显示
                String shortDesc = description != null ? 
                    (description.length() > 18 ? description.substring(0, 15) + "..." : description) : "N/A";
                
                log.info("{:<15} {:<20} {:<10} {:<15} {:<15} {:<20}", 
                        partitionName, 
                        shortDesc,
                        tableRows,
                        formatBytes(avgRowLength),
                        formatBytes(dataLength),
                        createTime != null ? createTime.toString() : "N/A");
            }
            
            log.info("{}","-".repeat(100));
            log.info("总计: 行数={}, 数据大小={}", totalRows, formatBytes(totalDataLength));
            
            // 检查分区健康状态
            checkPartitionHealth(partitions);
            
            log.info("=== 分区状态监控完成 ===");
            
        } catch (Exception e) {
            log.error("监控分区状态失败", e);
            throw new RuntimeException("监控分区状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查分区健康状态
     */
    private void checkPartitionHealth(List<Map<String, Object>> partitions) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate futureThreshold = today.plusDays(30); // 未来30天
            LocalDate pastThreshold = today.minusDays(30);   // 过去30天
            
            boolean hasFuturePartition = false;
            int recentPartitions = 0;
            int oldPartitions = 0;
            
            for (Map<String, Object> partition : partitions) {
                String partitionName = (String) partition.get("PARTITION_NAME");
                
                if ("p_future".equals(partitionName)) {
                    hasFuturePartition = true;
                    continue;
                }
                
                // 尝试从分区名解析日期
                try {
                    if (partitionName.startsWith("p") && partitionName.length() >= 7) {
                        String dateStr = partitionName.substring(1);
                        LocalDate partitionDate;
                        
                        if (dateStr.length() == 6) { // YYYYMM格式
                            partitionDate = LocalDate.parse(dateStr + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));
                        } else if (dateStr.length() == 8) { // YYYYMMDD格式
                            partitionDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                        } else {
                            continue;
                        }
                        
                        if (partitionDate.isAfter(pastThreshold)) {
                            recentPartitions++;
                        } else {
                            oldPartitions++;
                        }
                    }
                } catch (Exception e) {
                    log.debug("无法解析分区日期: {}", partitionName);
                }
            }
            
            log.info("=== 分区健康检查 ===");
            log.info("未来分区存在: {}", hasFuturePartition ? "是" : "否");
            log.info("近期分区数量: {}", recentPartitions);
            log.info("历史分区数量: {}", oldPartitions);
            
            // 健康状态警告
            if (!hasFuturePartition) {
                log.warn("警告: 未找到p_future分区，可能影响新数据插入");
            }
            
            if (recentPartitions == 0) {
                log.warn("警告: 未找到近期分区，可能需要创建当前月份分区");
            }
            
            if (oldPartitions > 6) {
                log.warn("警告: 历史分区过多({}个)，建议清理", oldPartitions);
            }
            
        } catch (Exception e) {
            log.error("分区健康检查失败", e);
        }
    }

    /**
     * 格式化字节数为可读格式
     */
    private String formatBytes(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f%s", size, units[unitIndex]);
    }
}
