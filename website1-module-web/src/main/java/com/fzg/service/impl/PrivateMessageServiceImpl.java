package com.fzg.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.PrivateConversationMapper;
import com.fzg.mapper.PrivateMessageMapper;
import com.fzg.mapper.UserMapper;
import com.fzg.model.PrivateConversation;
import com.fzg.model.PrivateMessage;
import com.fzg.model.User;
import com.fzg.service.PrivateMessageService;
import com.fzg.vo.ConversationVO;
import com.fzg.websocket.WebSocketManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage>
        implements PrivateMessageService {

    @Autowired
    private PrivateConversationMapper conversationMapper;

    @Autowired
    private PrivateMessageMapper messageMapper;
    @Autowired
    private UserMapper userMapper; // 查用户信息

    @Override
    public PrivateConversation getConversationById(Long conversationId) {
        return conversationMapper.selectById(conversationId);
    }

    @Override
    public List<ConversationVO> getConversationList(Long userId) {
        return conversationMapper.selectConversationList(userId);
    }
    /**
     * 获取或创建会话
     */
    @Override
    public PrivateConversation getOrCreateConversation(Long userId, Long targetId) {

        Long min = Math.min(userId, targetId);
        Long max = Math.max(userId, targetId);

        PrivateConversation conv = conversationMapper.selectByUsers(min, max);

        if (conv == null) {
            conv = new PrivateConversation();
            conv.setUserMin(min);
            conv.setUserMax(max);
            conversationMapper.insert(conv);
        }

        return conv;
    }

    /**
     * 发送消息（核心）
     */
    @Transactional
    @Override
    public PrivateMessage sendMessage(Long senderId, Long receiverId,
                                      String content, String contentType) {

        // 1. 会话
        PrivateConversation conv = getOrCreateConversation(senderId, receiverId);

        // 2. 消息
        PrivateMessage msg = new PrivateMessage();
        msg.setConversationId(conv.getId());
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setContentType(contentType);

        log.info("消息：{}",JSON.toJSONString(msg));
        messageMapper.insert(msg);

        // 3. 更新会话
        conversationMapper.updateLastMessage(
                conv.getId(),
                msg.getId(),
                content
        );

        // 4. 查询发送者信息
        User sender = userMapper.selectById(senderId);
        log.info("查询发送者信息：{}",JSON.toJSONString(sender));
        // 5. 推送给接收方
        pushToUser(receiverId, msg, sender, conv.getId());

        // 6. 推送给自己（多端同步）
        pushToUser(senderId, msg, sender, conv.getId());

        return msg;
    }

    private void pushToUser(Long userId, PrivateMessage msg, User sender, Long convId) {

        // 1. 推送消息
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("type", "chat_message");
        messageData.put("conversationId", convId);

        Map<String, Object> msgBody = new HashMap<>();
        msgBody.put("id", msg.getId());
        msgBody.put("senderId", msg.getSenderId());
        msgBody.put("receiverId", msg.getReceiverId());
        msgBody.put("content", msg.getContent());
        msgBody.put("contentType", msg.getContentType());
        msgBody.put("createdAt", msg.getCreatedAt());

        messageData.put("message", msgBody);

        WebSocketManager.sendMessageToUser(userId, JSON.toJSONString(messageData));

        // 2. 推送会话更新（重点）
        Map<String, Object> convData = new HashMap<>();
        convData.put("type", "conversation_update");

        Map<String, Object> convBody = new HashMap<>();
        convBody.put("conversationId", convId);
        convBody.put("targetUserId", sender.getId());
        convBody.put("nickname", sender.getNickname());
        convBody.put("avatar", sender.getAvatar());
        convBody.put("lastMessage", msg.getContent());
        convBody.put("lastMessageTime", msg.getCreatedAt());

        convData.put("conversation", convBody);

        WebSocketManager.sendMessageToUser(userId, JSON.toJSONString(convData));

        // 3. 推送未读数
        int totalUnread = messageMapper.countUnread(userId);

        Map<String, Object> unreadData = new HashMap<>();
        unreadData.put("type", "unread_update");
        unreadData.put("totalUnread", totalUnread);

        WebSocketManager.sendMessageToUser(userId, JSON.toJSONString(unreadData));
    }

    /**
     * 获取消息列表
     */
    @Override
    public Map<String, Object> getMessages(Long conversationId, Long userId,
                                           int pageNum, int pageSize) {

        int offset = (pageNum - 1) * pageSize;

        List<PrivateMessage> list = messageMapper.selectByConversation(
                conversationId, offset, pageSize
        );

        int total = messageMapper.countByConversation(conversationId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);

        return result;
    }

    /**
     * 会话列表
     */
    @Override
    public List<PrivateConversation> getConversations(Long userId, int pageNum, int pageSize) {

        int offset = (pageNum - 1) * pageSize;

        return conversationMapper.selectByUser(userId, offset, pageSize);
    }

    /**
     * 标记已读
     */
    @Override
    public int markRead(Long conversationId, Long userId) {

        return messageMapper.markRead(conversationId, userId);
    }

    /**
     * 未读总数
     */
    @Override
    public int getTotalUnread(Long userId) {
        return messageMapper.countUnread(userId);
    }

}
