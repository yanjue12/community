package com.fzg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Article;
import com.fzg.model.Notification;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 * 职责：通知查询、管理、缓存等业务逻辑
 */
public interface INotificationService extends IService<Notification> {

    /**
     * 获取未读数量（带缓存）
     */
    Long getUnreadCount(Long userId);

    /**
     * 获取各类型未读数量
     */
    Map<String, Long> getUnreadCountByType(Long userId);

    /**
     * 标记单个为已读
     */
    boolean markAsRead(Long userId, Long notificationId);

    /**
     * 批量标记为已读
     */
    int markBatchAsRead(Long userId, List<Long> notificationIds);

    /**
     * 全部标记为已读
     */
    int markAllAsRead(Long userId, String type);

    /**
     * 删除单个通知（逻辑删除）
     */
    boolean deleteNotification(Long userId, Long notificationId);

    /**
     * 删除所有通知（逻辑删除）
     */
    int deleteAllNotifications(Long userId);

    /**
     * 批量删除通知
     */
    int deleteBatch(Long userId, List<Long> notificationIds);

    /**
     * 清空已读通知
     */
    int clearReadNotifications(Long userId);

    /**
     * 获取通知列表（分页）
     */
    Page<Notification> getNotificationList(Long userId, Integer pageNum, Integer pageSize, String type, String isRead);

    /**
     * 获取通知详情（自动标记为已读）
     */
    Notification getNotificationDetail(Long userId, Long notificationId);

    /**
     * 手动清理已读通知
     */
    int manualCleanupReadNotifications(Integer days);
}
