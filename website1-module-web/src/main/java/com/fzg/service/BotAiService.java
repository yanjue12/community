package com.fzg.service;

import com.fzg.vo.BotDrawingHistoryItemVO;
import com.fzg.vo.BotConversationDetailVO;
import com.fzg.vo.BotConversationSessionVO;
import com.fzg.vo.BotHistoryItemVO;

import java.util.List;
import java.util.Map;

public interface BotAiService {
    String chat(Long userId, String conversationId, String model, String message);

    void resetConversation(String conversationId);

    List<BotHistoryItemVO> getConversationHistory(String conversationId, Integer limit);

    List<BotConversationSessionVO> getConversationSessions(Long userId, Integer limit);

    BotConversationDetailVO getConversationDetail(Long userId, String conversationId);

    Map<String, Object> createMultimodalEmbedding(Long userId, String model, String text, String imageUrl, String prompt);

    List<BotDrawingHistoryItemVO> getDrawingHistory(Long userId, Integer limit);

    boolean deleteDrawingHistory(Long userId, String recordId);
}
