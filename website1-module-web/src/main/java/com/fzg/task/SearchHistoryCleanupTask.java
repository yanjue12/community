package com.fzg.task;

import com.fzg.mapper.SearchHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 搜索历史清理任务
 * 每天凌晨4点执行，每个用户只保留最近15条搜索历史，其余物理删除
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SearchHistoryCleanupTask {

    private final SearchHistoryMapper searchHistoryMapper;

    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupExceedingSearchHistory() {
        try {
            log.info("=== 开始执行搜索历史清理任务 ===");

            List<Long> userIds = searchHistoryMapper.selectDistinctUserIds();
            if (userIds == null || userIds.isEmpty()) {
                log.info("搜索历史清理任务完成，无需清理");
                return;
            }

            int totalDeleted = 0;
            for (Long userId : userIds) {
                // 取该用户最近15条的ID
                List<Long> keepIds = searchHistoryMapper.selectTop15IdsByUser(userId);
                // 删除不在保留列表中的记录
                int deleted = searchHistoryMapper.deleteExceedingHistory(userId, keepIds);
                totalDeleted += deleted;
            }

            log.info("搜索历史清理任务完成，共处理 {} 个用户，删除 {} 条超出记录", userIds.size(), totalDeleted);
        } catch (Exception e) {
            log.error("搜索历史清理任务执行失败: {}", e.getMessage(), e);
        }
    }
}
