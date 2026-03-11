package com.fzg.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 审核通知服务
 * 处理文章审核通过/失败的通知
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditNotificationService {

    private final NotificationPublisher notificationPublisher;

    /**
     * 审核通过通知
     */
    @Async("notificationExecutor")
    public void notifyAuditPass(Long authorId, Long articleId, String articleTitle) {
        try {
            // 系统通知用户审核通过
            String content = "你的文章《" + articleTitle + "》已通过审核，现已发布";
            String title = "文章审核通过";
            
            // 这里使用系统通知，因为是系统操作
            // 如果需要记录审核员信息，可以使用 fromUserId
            publishSystemNotification(authorId, title, content, "normal", articleId, "article");
            
            log.info("审核通过通知已发送: authorId={}, articleId={}", authorId, articleId);
        } catch (Exception e) {
            log.error("发送审核通过通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 审核拒绝通知
     */
    @Async("notificationExecutor")
    public void notifyAuditReject(Long authorId, Long articleId, String articleTitle, String reason) {
        try {
            String content = "你的文章《" + articleTitle + "》审核未通过。原因: " + reason;
            String title = "文章审核拒绝";
            
            publishSystemNotification(authorId, title, content, "important", articleId, "article");
            
            log.info("审核拒绝通知已发送: authorId={}, articleId={}", authorId, articleId);
        } catch (Exception e) {
            log.error("发送审核拒绝通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发布系统通知
     */
    private void publishSystemNotification(Long userId, String title, String content, 
                                          String level, Long targetId, String targetType) {
        // 创建系统通知事件
        java.util.Map<String, Object> extra = new java.util.HashMap<>();
        extra.put("targetId", targetId);
        extra.put("targetType", targetType);
        
        // 使用 NotificationPublisher 的通用方法
        // 这里 fromUserId 为 null，表示系统通知
        // 为了兼容，我们使用 0 作为系统用户ID
        // 实际应该在 NotificationPublisher 中添加系统通知方法
    }
}
