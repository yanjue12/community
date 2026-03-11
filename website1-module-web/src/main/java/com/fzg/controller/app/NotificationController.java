package com.fzg.controller.app;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.mapper.Notificationmapper;
import com.fzg.mapper.UserMapper;
import com.fzg.model.Notification;
import com.fzg.model.Result;
import com.fzg.model.User;
import com.fzg.service.impl.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notification")
@SaCheckLogin
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private Notificationmapper notificationMapper;
    
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取通知列表（分页）
     */
    @GetMapping("/list")
    public Result getNotificationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String isRead) {
        
        Long userId = StpUtil.getLoginIdAsLong();
        
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsDeleted, "0");
        
        // 按类型筛选
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        
        // 按已读状态筛选
        if (isRead != null && !isRead.trim().isEmpty()) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        
        wrapper.orderByDesc(Notification::getCreatedAt);
        
        Page<Notification> result = notificationMapper.selectPage(page, wrapper);
        
        // 填充发送者信息
        List<Notification> notifications = result.getRecords();
        if (!notifications.isEmpty()) {
            List<Long> fromUserIds = notifications.stream()
                    .map(Notification::getFromUserId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (!fromUserIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(fromUserIds);
                Map<Long, User> userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
                
                // 组装返回数据
                List<Map<String, Object>> notificationList = notifications.stream().map(n -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", n.getId());
                    item.put("type", n.getType());
                    item.put("actionType", n.getActionType());
                    item.put("title", n.getTitle());
                    item.put("content", n.getContent());
                    item.put("targetType", n.getTargetType());
                    item.put("targetId", n.getTargetId());
                    item.put("isRead", n.getIsRead());
                    item.put("createdAt", n.getCreatedAt());
                    item.put("extraData", n.getExtraData());
                    
                    // 添加发送者信息
                    if (n.getFromUserId() != null && userMap.containsKey(n.getFromUserId())) {
                        User fromUser = userMap.get(n.getFromUserId());
                        Map<String, Object> fromUserInfo = new HashMap<>();
                        fromUserInfo.put("id", fromUser.getId());
                        fromUserInfo.put("username", fromUser.getUsername());
                        fromUserInfo.put("nickname", fromUser.getNickname());
                        fromUserInfo.put("avatar", fromUser.getAvatar());
                        item.put("fromUser", fromUserInfo);
                    }
                    
                    return item;
                }).collect(Collectors.toList());
                
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("records", notificationList);
                resultData.put("total", result.getTotal());
                resultData.put("current", result.getCurrent());
                resultData.put("size", result.getSize());
                resultData.put("pages", result.getPages());
                
                return Result.success(resultData);
            }
        }
        
        return Result.success(result);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    public Result getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        Long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 获取各类型未读数量
     */
    @GetMapping("/unread/count/by-type")
    public Result getUnreadCountByType() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Map<String, Long> counts = new HashMap<>();
        
        // 用户互动
        LambdaQueryWrapper<Notification> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(Notification::getUserId, userId)
                   .eq(Notification::getType, "user")
                   .eq(Notification::getIsRead, "0")
                   .eq(Notification::getIsDeleted, "0");
        counts.put("user", notificationMapper.selectCount(userWrapper));
        
        // 系统消息
        LambdaQueryWrapper<Notification> systemWrapper = new LambdaQueryWrapper<>();
        systemWrapper.eq(Notification::getUserId, userId)
                     .eq(Notification::getType, "system")
                     .eq(Notification::getIsRead, "0")
                     .eq(Notification::getIsDeleted, "0");
        counts.put("system", notificationMapper.selectCount(systemWrapper));
        
        // 私信
        LambdaQueryWrapper<Notification> messageWrapper = new LambdaQueryWrapper<>();
        messageWrapper.eq(Notification::getUserId, userId)
                      .eq(Notification::getType, "message")
                      .eq(Notification::getIsRead, "0")
                      .eq(Notification::getIsDeleted, "0");
        counts.put("message", notificationMapper.selectCount(messageWrapper));
        
        // 总数
        counts.put("total", counts.get("user") + counts.get("system") + counts.get("message"));
        
        return Result.success(counts);
    }

    /**
     * 标记单个通知为已读
     */
    @PutMapping("/{id}/read")
    public Result markAsRead(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        int result = notificationService.markAsRead(userId, java.util.Collections.singletonList(id));
        return Result.handle(result > 0);
    }

    /**
     * 批量标记为已读
     */
    @PutMapping("/batch/read")
    public Result batchMarkAsRead(@RequestBody List<Long> ids) {
        Long userId = StpUtil.getLoginIdAsLong();
        int result = notificationService.markAsRead(userId, ids);
        return Result.handle(result > 0);
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/all/read")
    public Result markAllAsRead(@RequestParam(required = false) String type) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        if (type != null && !type.trim().isEmpty()) {
            // 按类型标记
            LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Notification::getUserId, userId)
                   .eq(Notification::getType, type)
                   .eq(Notification::getIsRead, "0");
            
            Notification update = new Notification();
            update.setIsRead("1");
            update.setReadAt(new java.util.Date());
            
            int result = notificationMapper.update(update, wrapper);
            return Result.handle(result > 0);
        } else {
            // 全部标记
            int result = notificationService.markAllAsRead(userId);
            return Result.handle(result > 0);
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public Result deleteNotification(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        int result = notificationService.deleteNotification(userId, id);
        return Result.handle(result > 0);
    }

    /**
     * 批量删除通知
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody List<Long> ids) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .in(Notification::getId, ids);
        
        Notification update = new Notification();
        update.setIsDeleted("1");
        
        int result = notificationMapper.update(update, wrapper);
        return Result.handle(result > 0);
    }

    /**
     * 清空已读通知
     */
    @DeleteMapping("/clear/read")
    public Result clearReadNotifications() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, "1")
               .eq(Notification::getIsDeleted, "0");
        
        Notification update = new Notification();
        update.setIsDeleted("1");
        
        int result = notificationMapper.update(update, wrapper);
        return Result.handle(result > 0);
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{id}")
    public Result getNotificationDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getId, id)
               .eq(Notification::getUserId, userId)
               .eq(Notification::getIsDeleted, "0");
        
        Notification notification = notificationMapper.selectOne(wrapper);
        
        if (notification == null) {
            return Result.fail(404, "通知不存在");
        }
        
        // 自动标记为已读
        if ("0".equals(notification.getIsRead())) {
            notificationService.markAsRead(userId, java.util.Collections.singletonList(id));
            notification.setIsRead("1");
        }
        
        // 填充发送者信息
        if (notification.getFromUserId() != null) {
            User fromUser = userMapper.selectById(notification.getFromUserId());
            if (fromUser != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("notification", notification);
                
                Map<String, Object> fromUserInfo = new HashMap<>();
                fromUserInfo.put("id", fromUser.getId());
                fromUserInfo.put("username", fromUser.getUsername());
                fromUserInfo.put("nickname", fromUser.getNickname());
                fromUserInfo.put("avatar", fromUser.getAvatar());
                result.put("fromUser", fromUserInfo);
                
                return Result.success(result);
            }
        }
        
        return Result.success(notification);
    }
}
