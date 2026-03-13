package com.fzg.controller.app;

import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
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

    /**
     * 获取在线用户数量
     */
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

    /**
     * 测试推送通知给指定用户
     */
    @PostMapping("/test/push/{userId}")
    @Operation(summary = "测试推送通知")
    public Result<String> testPushNotification(@PathVariable Long userId, 
                                             @RequestParam String title, 
                                             @RequestParam String content) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("test", true);
        
        boolean success = webSocketPushService.pushNotificationToUser(userId, "test", title, content, extraData);
        
        if (success) {
            return Result.success("测试通知推送成功");
        } else {
            return Result.fail(EnumReturn.valueOf("用户不在线或推送失败"));
        }
    }
}