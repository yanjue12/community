package com.fzg.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Notification;
import com.fzg.model.Result;
import com.fzg.service.INotificationService;
import com.fzg.service.WebSocketPushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知相关接口
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "通知管理", description = "通知相关接口")
public class NotificationController {

    private final WebSocketPushService webSocketPushService;
    private final INotificationService notificationService;

    /**
     * 获取用户通知列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户通知列表")
    public Result<Page<Notification>> getNotificationList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String isRead) {
        
        try {
            Page<Notification> notifications = notificationService.getNotificationList(userId, pageNum, pageSize, type, isRead);
            return Result.success(notifications);
        } catch (Exception e) {
            log.error("获取用户{}通知列表失败: {}", userId, e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取通知列表失败"));
        }
    }

    /**
     * 获取用户未读通知数量
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取用户未读通知数量")
    public Result<Long> getUnreadNotificationCount(@RequestParam Long userId) {
        try {
            Long count = notificationService.getUnreadCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取用户{}未读通知数量失败: {}", userId, e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取未读通知数量失败"));
        }
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/mark-read/{notificationId}")
    @Operation(summary = "标记通知为已读")
    public Result<String> markNotificationAsRead(@PathVariable Long notificationId, @RequestParam Long userId) {
        try {
            boolean success = notificationService.markAsRead(userId, notificationId);
            if (success) {
                return Result.success("标记成功");
            } else {
                return Result.fail(EnumReturn.valueOf("标记失败"));
            }
        } catch (Exception e) {
            log.error("标记通知{}为已读失败: {}", notificationId, e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("标记失败"));
        }
    }

    /**
     * 批量标记通知为已读
     */
    @PostMapping("/mark-read-batch")
    @Operation(summary = "批量标记通知为已读")
    public Result<String> markNotificationsAsRead(@RequestParam Long userId, @RequestParam(required = false) String type) {
        try {
            int count = notificationService.markAllAsRead(userId, type);
            return Result.success("成功标记" + count + "条通知为已读");
        } catch (Exception e) {
            log.error("批量标记用户{}通知为已读失败: {}", userId, e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("批量标记失败"));
        }
    }

    /**
     * 获取所有在线用户列表（调试用）
     */
    @GetMapping("/online-users")
    @Operation(summary = "获取所有在线用户列表")
    public Result<Map<String, Object>> getOnlineUsers() {
        try {
            int count = webSocketPushService.getOnlineUserCount();
            // 获取在线用户ID列表
            java.util.Set<Long> onlineUserIds = com.fzg.websocket.WebSocketManager.getOnlineUsers().keySet();
            
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            result.put("userIds", onlineUserIds);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取在线用户列表失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取在线用户列表失败"));
        }
    }


    @GetMapping("/online-count")
    @Operation(summary = "获取在线用户数量")
    public Result<Integer> getOnlineUserCount() {
        int count = webSocketPushService.getOnlineUserCount();
        return Result.success(count);
    }

    /**
     * 检查用户是否在线
     */
    @GetMapping("/online-status/{userId}")
    @Operation(summary = "检查用户是否在线")
    public Result<Map<String, Object>> checkUserOnlineStatus(@PathVariable Long userId) {
        boolean isOnline = webSocketPushService.isUserOnline(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("isOnline", isOnline);
        return Result.success(result);
    }

    /**
     * 推送系统通知给所有在线用户（管理员功能）
     */
    @PostMapping("/system/broadcast")
    @Operation(summary = "推送系统通知")
    public Result<String> broadcastSystemNotification(@RequestParam String title, @RequestParam String content) {
        webSocketPushService.pushSystemNotificationToAll(title, content);
        return Result.success("系统通知推送成功");
    }


}