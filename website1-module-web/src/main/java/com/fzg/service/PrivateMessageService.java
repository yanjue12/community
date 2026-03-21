package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.PrivateConversation;
import com.fzg.model.PrivateMessage;
import com.fzg.vo.ConversationVO;

import java.util.List;
import java.util.Map;


public interface PrivateMessageService extends IService<PrivateMessage> {
    PrivateConversation getOrCreateConversation(Long userId, Long targetId);

    PrivateMessage sendMessage(Long senderId, Long receiverId,
                               String content, String contentType);

    Map<String, Object> getMessages(Long conversationId, Long userId,
                                    int pageNum, int pageSize);

    List<PrivateConversation> getConversations(Long userId, int pageNum, int pageSize);

    int markRead(Long conversationId, Long userId);

    int getTotalUnread(Long userId);

    List<ConversationVO> getConversationList(Long userId);

    PrivateConversation getConversationById(Long conversationId);
}
