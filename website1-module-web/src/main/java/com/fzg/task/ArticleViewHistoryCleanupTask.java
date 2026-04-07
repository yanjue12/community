package com.fzg.task;

import com.fzg.mapper.ArticleViewHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * 文章浏览历史清理任务
 * 每天凌晨2点执行，物理删除7天前的浏览记录
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ArticleViewHistoryCleanupTask {

    private final ArticleViewHistoryMapper articleViewHistoryMapper;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredViewHistory() {
        try {
            log.info("=== 开始执行浏览历史清理任务 ===");

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -7);
            Date cutoffTime = cal.getTime();

            int deleted = articleViewHistoryMapper.deleteBeforeDate(cutoffTime);

            log.info("浏览历史清理完成，共删除 {} 条7天前的记录", deleted);
        } catch (Exception e) {
            log.error("浏览历史清理任务执行失败: {}", e.getMessage(), e);
        }
    }
}
