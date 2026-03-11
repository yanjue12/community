package com.fzg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.model.Notification;
import com.fzg.model.Result;
import com.fzg.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notification")
@Api(tags = "通知管理")
@Slf4j
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    @ApiOperation("获取通知列表")
    public Result<Page<Notification>> getNotificationList(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String isRead) {
        
        // TODO: 从token中获取userId，这里仅示例
        if (userId == null) {
            return Result.fail("用户ID不能为空");
        }
        
        Page<Notification> page = notificationService.getNotificationList(userId, pageNum, pageSize, type, isRead);
        return Result.success(page);
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread/count")
    @ApiOperation("获取未读数量")
    public Result<Long> getUnreadCount(@RequestParam Long userId) {
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 获取各类型未读数量
     */
    @GetMapping("/unread/count/by-type")
    @ApiOperation("获取各类型未读数量")
    public Result<Map<String, Long>> getUnreadCountByType(@RequestParam Long userId) {
        Map<String, Long> countMap = notificationService.getUnreadCountByType(userId);
        return Result.success(countMap);
    }

    /**
     * 标记单个为已读
     */
    @PutMapping("/{id}/read")
    @ApiOperation("标记单个为已读")
    public Result<Boolean> markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        boolean result = notificationService.markAsRead(userId, id);
        return Result.success(result);
    }

    /**
     * 批量标记为已读
     */
    @PutMapping("/batch/read")
    @ApiOperation("批量标记为已读")
    public Result<Integer> markBatchAsRead(@RequestParam Long userId, @RequestBody List<Long> notificationIds) {
        int result = notificationService.markBatchAsRead(userId, notificationIds);
        return Result.success(result);
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/all/read")
    @ApiOperation("全部标记为已读")
    public Result<Integer> markAllAsRead(@RequestParam Long userId, @RequestParam(required = false) String type) {
        int result = notificationService.markAllAsRead(userId, type);
        return Result.success(result);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除通知")
    public Result<Boolean> deleteNotification(@PathVariable Long id, @RequestParam Long userId) {
        boolean result = notificationService.deleteNotification(userId, id);
        return Result.success(result);
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch")
    @ApiOperation("批量删除通知")
    public Result<Integer> deleteBatch(@RequestParam Long userId, @RequestBody List<Long> notificationIds) {
        int result = notificationService.deleteBatch(userId, notificationIds);
        return Result.success(result);
    }

    /**
     * 清空已读通知
     */
    @DeleteMapping("/clear/read")
    @ApiOperation("清空已读通知")
    public Result<Integer> clearReadNotifications(@RequestParam Long userId) {
        int result = notificationService.clearReadNotifications(userId);
        return Result.success(result);
    }

    /**
     * 获取通知详情（自动标记为已读）
     */
    @GetMapping("/{id}")
    @ApiOperation("获取通知详情")
    public Result<Notification> getNotificationDetail(@PathVariable Long id, @RequestParam Long userId) {
        Notification notification = notificationService.getNotificationDetail(userId, id);
        if (notification == null) {
            return Result.fail("通知不存在");
        }
        return Result.success(notification);
    }
}
