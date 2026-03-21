package com.fzg.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.fzg.enums.EnumReturn;
import com.fzg.model.PrivateConversation;
import com.fzg.model.PrivateMessage;
import com.fzg.model.Result;
import com.fzg.service.PrivateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 用户私信
 */
@RestController
@RequestMapping("/message")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;

    /**
     * 获取会话列表（带头像+昵称+未读数）
     */
    @GetMapping("/conversations")
    public Result getConversationList() {
        Long userId = getUserId();
        return Result.success(privateMessageService.getConversationList(userId));
    }

    /**
     * 获取或创建会话
     */
    @GetMapping("/conversation/{targetUserId}")
    public Result getOrCreateConversation(@PathVariable Long targetUserId) {

        Long userId = getUserId();

        if (userId.equals(targetUserId)) {
            return Result.fail(EnumReturn.valueOf("不能和自己聊天"));
        }

        PrivateConversation conv =
                privateMessageService.getOrCreateConversation(userId, targetUserId);

        return Result.success(conv);
    }

    /**
     * 获取消息列表
     */
    @GetMapping("/list/{conversationId}")
    public Result getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {

        Long userId = getUserId();

        //权限校验（必须加）
        checkConversationPermission(conversationId, userId);

        Map<String, Object> data =
                privateMessageService.getMessages(conversationId, userId, pageNum, pageSize);

        // 自动标记已读
        privateMessageService.markRead(conversationId, userId);

        return Result.success(data);
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result sendMessage(@RequestBody Map<String, Object> params) {

        Long senderId = getUserId();

        Long receiverId = Long.valueOf(params.get("receiverId").toString());
        String content = (String) params.get("content");
        String contentType = (String) params.getOrDefault("contentType", "1");

        //基础校验
        if (receiverId == null) {
            return Result.fail(EnumReturn.valueOf("接收者不能为空"));
        }

        if (senderId.equals(receiverId)) {
            return Result.fail(EnumReturn.valueOf("不能给自己发消息"));
        }

        if (content == null || content.trim().isEmpty()) {
            return Result.fail(EnumReturn.valueOf("消息内容不能为空"));
        }

        if (content.length() > 2000) {
            return Result.fail(EnumReturn.valueOf("消息长度过长"));
        }

        PrivateMessage msg = privateMessageService.sendMessage(
                senderId,
                receiverId,
                content,
                contentType
        );

        return Result.success(msg);
    }

    /**
     * 标记已读
     */
    @PostMapping("/read/{conversationId}")
    public Result markRead(@PathVariable Long conversationId) {

        Long userId = getUserId();

        checkConversationPermission(conversationId, userId);

        int i = privateMessageService.markRead(conversationId, userId);

        return Result.handle(i > 0);
    }

    /**
     * 获取未读总数
     */
    @GetMapping("/unread")
    public Result getTotalUnread() {
        Long userId = getUserId();
        return Result.success(privateMessageService.getTotalUnread(userId));
    }

    // ================== 工具方法 ==================

    private Long getUserId() {
        return Long.valueOf(StpUtil.getLoginId().toString());
    }

    /**
     * 会话权限校验（非常关键）
     */
    private void checkConversationPermission(Long conversationId, Long userId) {

        PrivateConversation conv =
                privateMessageService.getConversationById(conversationId);

        if (conv == null) {
            throw new RuntimeException("会话不存在");
        }

        if (!userId.equals(conv.getUserMin())
                && !userId.equals(conv.getUserMax())) {

            throw new RuntimeException("无权限访问该会话");
        }
    }
}