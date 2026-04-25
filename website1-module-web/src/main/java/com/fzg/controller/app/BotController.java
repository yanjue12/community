package com.fzg.controller.app;

import com.fzg.model.Result;
import com.fzg.service.BotAiService;
import com.fzg.config.ArkAiProperties;
import com.fzg.vo.BotChatResponseVO;
import com.fzg.vo.BotConversationDetailVO;
import com.fzg.vo.BotConversationSessionVO;
import com.fzg.vo.BotErrorVO;
import com.fzg.vo.BotChatRequest;
import com.fzg.vo.BotHistoryItemVO;
import com.fzg.vo.BotMultimodalEmbeddingRequest;
import com.fzg.vo.BotDrawingHistoryItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/bot")
@RequiredArgsConstructor
@Slf4j
public class BotController {

    private final BotAiService botAiService;
    private final ArkAiProperties arkAiProperties;

    @PostMapping("/chat")
    public Result<?> chat(@RequestBody BotChatRequest request) {
        String requestId = UUID.randomUUID().toString();
        String conversationId = request == null || StringUtils.isBlank(request.getConversationId())
                ? UUID.randomUUID().toString()
                : request.getConversationId();
        String model = request == null || StringUtils.isBlank(request.getModel())
                ? arkAiProperties.getModel()
                : request.getModel();
        long timestamp = System.currentTimeMillis();
        if (request == null || StringUtils.isBlank(request.getMessage())) {
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("INVALID_REQUEST");
            errorVO.setErrorMessage("消息不能为空");
            return new Result<>(400, "请求参数错误", errorVO);
        }
        try {
            String answer = botAiService.chat(request.getUserId(), conversationId, model, request.getMessage());
            BotChatResponseVO responseVO = new BotChatResponseVO();
            responseVO.setRequestId(requestId);
            responseVO.setConversationId(conversationId);
            responseVO.setTimestamp(timestamp);
            responseVO.setModel(model);
            responseVO.setAnswer(answer);
            return Result.success(responseVO);
        } catch (Exception e) {
            log.error("机器人对话失败 requestId={} err={}", requestId, e.getMessage(), e);
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("AI_SERVICE_ERROR");
            errorVO.setErrorMessage(e.getMessage());
            return new Result<>(500, "AI服务调用失败", errorVO);
        }
    }

    @GetMapping("/chat/history")
    public Result<?> history(@RequestParam String conversationId,
                             @RequestParam(required = false, defaultValue = "20") Integer limit) {
        String requestId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        if (StringUtils.isBlank(conversationId)) {
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("INVALID_REQUEST");
            errorVO.setErrorMessage("conversationId 不能为空");
            return new Result<>(400, "请求参数错误", errorVO);
        }
        try {
            List<BotHistoryItemVO> history = botAiService.getConversationHistory(conversationId, limit);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取会话历史失败 requestId={} err={}", requestId, e.getMessage(), e);
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("GET_HISTORY_ERROR");
            errorVO.setErrorMessage(e.getMessage());
            return new Result<>(500, "获取会话历史失败", errorVO);
        }
    }

    @GetMapping("/chat/sessions")
    public Result<?> sessions(@RequestParam(required = false) Long userId,
                              @RequestParam(required = false, defaultValue = "20") Integer limit) {
        String requestId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        try {
            List<BotConversationSessionVO> sessions = botAiService.getConversationSessions(userId, limit);
            return Result.success(sessions);
        } catch (Exception e) {
            log.error("获取会话列表失败 requestId={} err={}", requestId, e.getMessage(), e);
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("GET_SESSIONS_ERROR");
            errorVO.setErrorMessage(e.getMessage());
            return new Result<>(500, "获取会话列表失败", errorVO);
        }
    }

    @GetMapping("/chat/session/detail")
    public Result<?> sessionDetail(@RequestParam(required = false) Long userId,
                                   @RequestParam String conversationId) {
        String requestId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        if (StringUtils.isBlank(conversationId)) {
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("INVALID_REQUEST");
            errorVO.setErrorMessage("conversationId 不能为空");
            return new Result<>(400, "请求参数错误", errorVO);
        }
        try {
            BotConversationDetailVO detail = botAiService.getConversationDetail(userId, conversationId);
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取会话详情失败 requestId={} err={}", requestId, e.getMessage(), e);
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("GET_SESSION_DETAIL_ERROR");
            errorVO.setErrorMessage(e.getMessage());
            return new Result<>(500, "获取会话详情失败", errorVO);
        }
    }

    @PostMapping("/chat/reset")
    public Result<?> resetConversation(@RequestParam String conversationId) {
        String requestId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        if (StringUtils.isBlank(conversationId)) {
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("INVALID_REQUEST");
            errorVO.setErrorMessage("conversationId 不能为空");
            return new Result<>(400, "请求参数错误", errorVO);
        }
        try {
            botAiService.resetConversation(conversationId);
            return Result.success(true);
        } catch (Exception e) {
            log.error("清空会话上下文失败 requestId={} err={}", requestId, e.getMessage(), e);
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("RESET_CONTEXT_ERROR");
            errorVO.setErrorMessage(e.getMessage());
            return new Result<>(500, "清空会话上下文失败", errorVO);
        }
    }

    @PostMapping("/embedding/multimodal")
    public Result<?> multimodalEmbedding(@RequestBody BotMultimodalEmbeddingRequest request) {
        String requestId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        if (request == null || (StringUtils.isBlank(request.getText()) && StringUtils.isBlank(request.getImageUrl()))) {
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("INVALID_REQUEST");
            errorVO.setErrorMessage("text 和 imageUrl 不能同时为空");
            return new Result<>(400, "请求参数错误", errorVO);
        }
        try {
            Map<String, Object> result = botAiService.createMultimodalEmbedding(
                    request.getUserId(),
                    request.getModel(),
                    request.getText(),
                    request.getImageUrl(),
                    request.getPrompt()
            );
            return Result.success(result);
        } catch (Exception e) {
            log.error("多模态Embedding失败 requestId={} err={}", requestId, e.getMessage(), e);
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("MULTIMODAL_EMBEDDING_ERROR");
            errorVO.setErrorMessage(e.getMessage());
            return new Result<>(500, "多模态Embedding失败", errorVO);
        }
    }

    @GetMapping("/drawing/history")
    public Result<?> drawingHistory(@RequestParam Long userId,
                                    @RequestParam(required = false, defaultValue = "20") Integer limit) {
        String requestId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        if (userId == null) {
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("INVALID_REQUEST");
            errorVO.setErrorMessage("userId 不能为空");
            return new Result<>(400, "请求参数错误", errorVO);
        }
        try {
            List<BotDrawingHistoryItemVO> history = botAiService.getDrawingHistory(userId, limit);
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取会话历史失败 requestId={} err={}", requestId, e.getMessage(), e);
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("GET_DRAWING_HISTORY_ERROR");
            errorVO.setErrorMessage(e.getMessage());
            return new Result<>(500, "获取会话历史失败", errorVO);
        }
    }

    @PostMapping("/drawing/history/delete")
    public Result<?> deleteDrawingHistory(@RequestParam Long userId,
                                          @RequestParam String recordId) {
        String requestId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        if (userId == null || StringUtils.isBlank(recordId)) {
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("INVALID_REQUEST");
            errorVO.setErrorMessage("userId 和 recordId 不能为空");
            return new Result<>(400, "请求参数错误", errorVO);
        }
        try {
            boolean deleted = botAiService.deleteDrawingHistory(userId, recordId);
            return Result.success(deleted);
        } catch (Exception e) {
            log.error("删除会话历史失败 requestId={} err={}", requestId, e.getMessage(), e);
            BotErrorVO errorVO = new BotErrorVO();
            errorVO.setRequestId(requestId);
            errorVO.setTimestamp(timestamp);
            errorVO.setErrorCode("DELETE_DRAWING_HISTORY_ERROR");
            errorVO.setErrorMessage(e.getMessage());
            return new Result<>(500, "删除会话历史失败", errorVO);
        }
    }

    @GetMapping("/models")
    public Result<?> models() {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("defaultModel", arkAiProperties.getModel());
        data.put("defaultEmbeddingModel", arkAiProperties.getEmbeddingModel());
        data.put("allowedChatModels", arkAiProperties.getAllowedChatModels());
        data.put("allowedEmbeddingModels", arkAiProperties.getAllowedEmbeddingModels());
        data.put("allowedModels", arkAiProperties.getAllowedModels());
        return Result.success(data);
    }
}
