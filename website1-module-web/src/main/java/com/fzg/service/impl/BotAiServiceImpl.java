package com.fzg.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fzg.config.ArkAiProperties;
import com.fzg.service.BotAiService;
import com.fzg.vo.BotConversationDetailVO;
import com.fzg.vo.BotConversationSessionVO;
import com.fzg.vo.BotDrawingHistoryItemVO;
import com.fzg.vo.BotHistoryItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotAiServiceImpl implements BotAiService {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String HEADER_API_KEY = "X-API-Key";
    private static final String CONTEXT_KEY_PREFIX = "bot:context:";
    private static final String SESSION_META_KEY_PREFIX = "bot:session:meta:";
    private static final String USER_SESSIONS_KEY_PREFIX = "bot:user:sessions:";
    private static final String DRAWING_HISTORY_KEY_PREFIX = "bot:drawing:history:";

    private final ArkAiProperties arkAiProperties;
    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String chat(Long userId, String conversationId, String model, String message, String depth) {
        if (Boolean.FALSE.equals(arkAiProperties.getEnabled())) {
            throw new IllegalStateException("AI服务未启用");
        }
        if (StringUtils.isBlank(conversationId)) {
            throw new IllegalArgumentException("conversationId 不能为空");
        }
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        String finalModel = StringUtils.isBlank(model) ? arkAiProperties.getModel() : model;
        validateChatModel(finalModel);
        String finalDepth = StringUtils.isBlank(depth) ? arkAiProperties.getDefaultDepth() : depth;

        if (Boolean.TRUE.equals(arkAiProperties.getPlatformEnabled())) {
            try {
                String answer = callPlatformChat(userId, conversationId, finalModel, message, finalDepth);
                persistConversation(userId, conversationId, finalModel, message, answer);
                return answer;
            } catch (RuntimeException ex) {
                if (Boolean.TRUE.equals(arkAiProperties.getPlatformAllowDegrade())) {
                    log.warn("AI平台调用失败，执行降级返回 conversationId={} err={}", conversationId, ex.getMessage());
                    String degradedAnswer = "AI服务繁忙，已为你降级处理，请稍后再试。";
                    persistConversation(userId, conversationId, finalModel, message, degradedAnswer);
                    return degradedAnswer;
                }
                throw ex;
            }
        }

        if (StringUtils.isBlank(arkAiProperties.getApiKey())) {
            throw new IllegalStateException("ARK_API_KEY 未配置");
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(arkAiProperties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(arkAiProperties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(arkAiProperties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .build();

        String url = arkAiProperties.getBaseUrl() + "/chat/completions";
        List<Map<String, String>> fullHistoryMessages = loadHistoryMessages(conversationId);
        String answer;
        try {
            answer = callChatCompletions(client, url, finalModel, fullHistoryMessages, message);
        } catch (RuntimeException ex) {
            if (isTimeoutException(ex)) {
                log.warn("Ark AI首轮请求超时, 降级重试(仅当前问题), conversationId={}", conversationId);
                answer = callChatCompletions(client, url, finalModel, new ArrayList<>(), message);
            } else {
                throw ex;
            }
        }
        persistConversation(userId, conversationId, finalModel, message, answer);
        return answer;
    }

    private void persistConversation(Long userId,
                                     String conversationId,
                                     String model,
                                     String userMessage,
                                     String answer) {
        appendConversation(conversationId, "user", userMessage);
        appendConversation(conversationId, "assistant", answer);
        trimHistory(conversationId);
        upsertConversationSession(userId, conversationId, model, userMessage, answer);
    }

    private String callPlatformChat(Long userId,
                                    String conversationId,
                                    String model,
                                    String message,
                                    String depth) {
        String url = trimTrailingSlash(arkAiProperties.getPlatformBaseUrl()) + "/bot/chat";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(arkAiProperties.getPlatformConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(arkAiProperties.getPlatformReadTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(arkAiProperties.getPlatformReadTimeoutSeconds(), TimeUnit.SECONDS)
                .build();
        int retryCount = Math.max(0, arkAiProperties.getPlatformRetryCount() == null ? 0 : arkAiProperties.getPlatformRetryCount());
        RuntimeException lastError = null;
        for (int attempt = 0; attempt <= retryCount; attempt++) {
            try {
                String payload = objectMapper.createObjectNode()
                        .put("userId", userId == null ? 0L : userId)
                        .put("conversationId", conversationId)
                        .put("model", model)
                        .put("message", message)
                        .put("depth", depth)
                        .toString();
                RequestBody body = RequestBody.create(JSON, payload);
                Request.Builder requestBuilder = new Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .post(body);
                if (StringUtils.isNotBlank(arkAiProperties.getPlatformApiKey())) {
                    requestBuilder.addHeader(HEADER_API_KEY, arkAiProperties.getPlatformApiKey());
                }
                try (Response response = client.newCall(requestBuilder.build()).execute()) {
                    String responseBody = response.body() == null ? "" : response.body().string();
                    if (!response.isSuccessful()) {
                        log.warn("AI平台chat调用失败 attempt={} code={} body={}", attempt + 1, response.code(), responseBody);
                        if (response.code() >= 500 && attempt < retryCount) {
                            continue;
                        }
                        throw new RuntimeException("AI平台调用失败: " + response.code());
                    }
                    JsonNode root = objectMapper.readTree(responseBody);
                    if (root.path("code").asInt(500) != 200) {
                        String errMsg = root.path("message").asText("AI平台返回失败");
                        if (attempt < retryCount) {
                            continue;
                        }
                        throw new RuntimeException(errMsg);
                    }
                    String answer = root.path("data").path("answer").asText("");
                    if (StringUtils.isBlank(answer)) {
                        throw new RuntimeException("AI平台返回内容为空");
                    }
                    return answer;
                }
            } catch (SocketTimeoutException e) {
                log.warn("AI平台chat超时 attempt={} err={}", attempt + 1, e.getMessage());
                lastError = new RuntimeException("AI平台请求超时", e);
            } catch (IOException e) {
                log.warn("AI平台chat请求异常 attempt={} err={}", attempt + 1, e.getMessage());
                lastError = new RuntimeException("AI平台请求异常", e);
            }
            if (attempt >= retryCount && lastError != null) {
                throw lastError;
            }
        }
        throw new RuntimeException("AI平台调用失败");
    }

    private String callChatCompletions(OkHttpClient client,
                                       String url,
                                       String model,
                                       List<Map<String, String>> historyMessages,
                                       String message) {
        String payload = buildChatPayload(model, historyMessages, message);
        RequestBody body = RequestBody.create(JSON, payload);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + arkAiProperties.getApiKey())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                log.error("Ark AI调用失败, code={}, body={}", response.code(), responseBody);
                throw new RuntimeException("AI服务调用失败: " + response.code());
            }
            String answer = parseAssistantContent(responseBody);
            if (StringUtils.isBlank(answer)) {
                throw new RuntimeException("AI返回内容为空");
            }
            return answer;
        } catch (SocketTimeoutException e) {
            log.error("Ark AI请求超时: {}", e.getMessage());
            throw new RuntimeException("AI服务请求超时", e);
        } catch (IOException e) {
            log.error("Ark AI请求异常: {}", e.getMessage(), e);
            throw new RuntimeException("AI服务请求异常", e);
        }
    }

    private Map<String, Object> callPlatformMultimodalEmbedding(Long userId,
                                                                 String model,
                                                                 String text,
                                                                 String imageUrl,
                                                                 String prompt) {
        String url = trimTrailingSlash(arkAiProperties.getPlatformBaseUrl()) + "/bot/embedding/multimodal";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(arkAiProperties.getPlatformConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(arkAiProperties.getPlatformReadTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(arkAiProperties.getPlatformReadTimeoutSeconds(), TimeUnit.SECONDS)
                .build();
        int retryCount = Math.max(0, arkAiProperties.getPlatformRetryCount() == null ? 0 : arkAiProperties.getPlatformRetryCount());
        RuntimeException lastError = null;
        for (int attempt = 0; attempt <= retryCount; attempt++) {
            try {
                String payload = objectMapper.createObjectNode()
                        .put("userId", userId == null ? 0L : userId)
                        .put("model", model)
                        .put("text", StringUtils.defaultString(text))
                        .put("imageUrl", StringUtils.defaultString(imageUrl))
                        .put("prompt", StringUtils.defaultString(prompt))
                        .toString();
                RequestBody body = RequestBody.create(JSON, payload);
                Request.Builder requestBuilder = new Request.Builder()
                        .url(url)
                        .addHeader("Content-Type", "application/json")
                        .post(body);
                if (StringUtils.isNotBlank(arkAiProperties.getPlatformApiKey())) {
                    requestBuilder.addHeader(HEADER_API_KEY, arkAiProperties.getPlatformApiKey());
                }
                try (Response response = client.newCall(requestBuilder.build()).execute()) {
                    String responseBody = response.body() == null ? "" : response.body().string();
                    if (!response.isSuccessful()) {
                        log.warn("AI平台embedding调用失败 attempt={} code={} body={}", attempt + 1, response.code(), responseBody);
                        if (response.code() >= 500 && attempt < retryCount) {
                            continue;
                        }
                        throw new RuntimeException("Embedding服务调用失败: " + response.code());
                    }
                    JsonNode root = objectMapper.readTree(responseBody);
                    if (root.path("code").asInt(500) != 200) {
                        String errMsg = root.path("message").asText("Embedding服务调用失败");
                        if (attempt < retryCount) {
                            continue;
                        }
                        throw new RuntimeException(errMsg);
                    }
                    JsonNode dataNode = root.path("data");
                    return objectMapper.convertValue(dataNode, Map.class);
                }
            } catch (SocketTimeoutException e) {
                log.warn("AI平台embedding超时 attempt={} err={}", attempt + 1, e.getMessage());
                lastError = new RuntimeException("Embedding服务请求超时", e);
            } catch (IOException e) {
                log.warn("AI平台embedding请求异常 attempt={} err={}", attempt + 1, e.getMessage());
                lastError = new RuntimeException("Embedding服务请求异常", e);
            }
            if (attempt >= retryCount && lastError != null) {
                throw lastError;
            }
        }
        throw new RuntimeException("Embedding服务调用失败");
    }

    private boolean isTimeoutException(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor != null) {
            if (cursor instanceof SocketTimeoutException) {
                return true;
            }
            cursor = cursor.getCause();
        }
        return false;
    }

    private String buildMultimodalEmbeddingPayload(String model, String text, String imageUrl) {
        try {
            com.fasterxml.jackson.databind.node.ObjectNode root = objectMapper.createObjectNode();
            root.put("model", model);
            com.fasterxml.jackson.databind.node.ArrayNode input = objectMapper.createArrayNode();
            if (StringUtils.isNotBlank(text)) {
                input.add(objectMapper.createObjectNode()
                        .put("type", "text")
                        .put("text", text));
            }
            if (StringUtils.isNotBlank(imageUrl)) {
                com.fasterxml.jackson.databind.node.ObjectNode imageNode = objectMapper.createObjectNode();
                imageNode.put("type", "image_url");
                imageNode.set("image_url", objectMapper.createObjectNode().put("url", imageUrl));
                input.add(imageNode);
            }
            root.set("input", input);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Embedding请求参数构造失败");
        }
    }

    private Map<String, Object> parseMultimodalEmbeddingResponse(String responseBody, String model) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dataNode = root.path("data");
            JsonNode first = dataNode.isArray() && dataNode.size() > 0 ? dataNode.get(0) : null;
            List<Double> embedding = new ArrayList<>();
            if (first != null && first.path("embedding").isArray()) {
                for (JsonNode n : first.path("embedding")) {
                    embedding.add(n.asDouble());
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("model", model);
            result.put("dimension", embedding.size());
            result.put("embedding", embedding);
            result.put("raw", root);
            return result;
        } catch (Exception e) {
            log.error("Embedding响应解析失败: body={}", responseBody, e);
            throw new RuntimeException("Embedding响应解析失败");
        }
    }

    @Override
    public void resetConversation(String conversationId) {
        if (StringUtils.isBlank(conversationId)) {
            throw new IllegalArgumentException("conversationId 不能为空");
        }
        stringRedisTemplate.delete(buildContextKey(conversationId));
    }

    @Override
    public List<BotHistoryItemVO> getConversationHistory(String conversationId, Integer limit) {
        if (StringUtils.isBlank(conversationId)) {
            throw new IllegalArgumentException("conversationId 不能为空");
        }
        int finalLimit = (limit == null || limit <= 0) ? 20 : limit;
        if (finalLimit > 100) {
            finalLimit = 100;
        }
        List<BotHistoryItemVO> all = parseHistoryFromRedis(conversationId);
        if (all.size() <= finalLimit) {
            return all;
        }
        return new ArrayList<>(all.subList(all.size() - finalLimit, all.size()));
    }

    @Override
    public List<BotConversationSessionVO> getConversationSessions(Long userId, Integer limit) {
        long finalUserId = normalizeUserId(userId);
        int finalLimit = (limit == null || limit <= 0) ? 20 : limit;
        if (finalLimit > 100) {
            finalLimit = 100;
        }
        Set<String> conversationIds = stringRedisTemplate.opsForZSet()
                .reverseRange(buildUserSessionsKey(finalUserId), 0, finalLimit - 1);
        List<BotConversationSessionVO> result = new ArrayList<>();
        if (conversationIds == null || conversationIds.isEmpty()) {
            return result;
        }
        for (String conversationId : conversationIds) {
            BotConversationSessionVO session = getSessionMeta(finalUserId, conversationId);
            if (session == null) {
                continue;
            }
            result.add(session);
        }
        return result;
    }

    @Override
    public BotConversationDetailVO getConversationDetail(Long userId, String conversationId) {
        if (StringUtils.isBlank(conversationId)) {
            throw new IllegalArgumentException("conversationId 不能为空");
        }
        long finalUserId = normalizeUserId(userId);
        BotConversationSessionVO session = getSessionMeta(finalUserId, conversationId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在");
        }
        BotConversationDetailVO detailVO = new BotConversationDetailVO();
        detailVO.setSession(session);
        detailVO.setMessages(parseHistoryFromRedis(conversationId));
        return detailVO;
    }

    @Override
    public Map<String, Object> createMultimodalEmbedding(Long userId, String model, String text, String imageUrl, String prompt) {
        if (Boolean.FALSE.equals(arkAiProperties.getEnabled())) {
            throw new IllegalStateException("AI服务未启用");
        }
        if (StringUtils.isBlank(text) && StringUtils.isBlank(imageUrl)) {
            throw new IllegalArgumentException("text 和 imageUrl 不能同时为空");
        }
        String finalModel = StringUtils.isBlank(model) ? arkAiProperties.getEmbeddingModel() : model;
        validateEmbeddingModel(finalModel);

        if (Boolean.TRUE.equals(arkAiProperties.getPlatformEnabled())) {
            Map<String, Object> parsed = callPlatformMultimodalEmbedding(userId, finalModel, text, imageUrl, prompt);
            appendDrawingHistory(userId, finalModel, prompt, text, imageUrl);
            return parsed;
        }

        if (StringUtils.isBlank(arkAiProperties.getApiKey())) {
            throw new IllegalStateException("ARK_API_KEY 未配置");
        }
        String url = arkAiProperties.getBaseUrl() + "/embeddings/multimodal";

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(arkAiProperties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(arkAiProperties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(arkAiProperties.getTimeoutSeconds(), TimeUnit.SECONDS)
                .build();
        String payload = buildMultimodalEmbeddingPayload(finalModel, text, imageUrl);

        RequestBody body = RequestBody.create(JSON, payload);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + arkAiProperties.getApiKey())
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                log.error("Ark multimodal embedding 调用失败, code={}, body={}", response.code(), responseBody);
                throw new RuntimeException("Embedding服务调用失败: " + response.code());
            }
            Map<String, Object> parsed = parseMultimodalEmbeddingResponse(responseBody, finalModel);
            appendDrawingHistory(userId, finalModel, prompt, text, imageUrl);
            return parsed;
        } catch (IOException e) {
            log.error("Ark multimodal embedding 请求异常: {}", e.getMessage(), e);
            throw new RuntimeException("Embedding服务请求异常");
        }
    }

    @Override
    public List<BotDrawingHistoryItemVO> getDrawingHistory(Long userId, Integer limit) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        int finalLimit = (limit == null || limit <= 0) ? 20 : limit;
        if (finalLimit > 100) {
            finalLimit = 100;
        }
        String key = buildDrawingHistoryKey(userId);
        List<String> rawHistory = stringRedisTemplate.opsForList().range(key, 0, -1);
        List<BotDrawingHistoryItemVO> result = new ArrayList<>();
        if (rawHistory == null || rawHistory.isEmpty()) {
            return result;
        }
        for (String item : rawHistory) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            try {
                JsonNode node = objectMapper.readTree(item);
                BotDrawingHistoryItemVO vo = new BotDrawingHistoryItemVO();
                vo.setRecordId(node.path("recordId").asText(null));
                JsonNode userIdNode = node.path("userId");
                vo.setUserId(userIdNode.isMissingNode() ? null : userIdNode.asLong());
                vo.setModel(node.path("model").asText(null));
                vo.setPrompt(node.path("prompt").asText(null));
                vo.setText(node.path("text").asText(null));
                vo.setImageUrl(node.path("imageUrl").asText(null));
                JsonNode timestampNode = node.path("timestamp");
                vo.setTimestamp(timestampNode.isMissingNode() ? null : timestampNode.asLong());
                result.add(vo);
            } catch (Exception e) {
                log.warn("解析绘画历史失败 userId={}", userId, e);
            }
        }
        if (result.size() <= finalLimit) {
            return result;
        }
        return new ArrayList<>(result.subList(result.size() - finalLimit, result.size()));
    }

    @Override
    public boolean deleteDrawingHistory(Long userId, String recordId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (StringUtils.isBlank(recordId)) {
            throw new IllegalArgumentException("recordId 不能为空");
        }
        String key = buildDrawingHistoryKey(userId);
        List<String> rawHistory = stringRedisTemplate.opsForList().range(key, 0, -1);
        if (rawHistory == null || rawHistory.isEmpty()) {
            return false;
        }
        for (String item : rawHistory) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            try {
                JsonNode node = objectMapper.readTree(item);
                String id = node.path("recordId").asText();
                if (StringUtils.equals(id, recordId)) {
                    Long removed = stringRedisTemplate.opsForList().remove(key, 1, item);
                    return removed != null && removed > 0;
                }
            } catch (Exception e) {
                log.warn("解析绘画历史失败 userId={} recordId={}", userId, recordId, e);
            }
        }
        return false;
    }

    private String buildChatPayload(String model, List<Map<String, String>> historyMessages, String message) {
        try {
            JsonNode root = objectMapper.createObjectNode()
                    .put("model", model);
            com.fasterxml.jackson.databind.node.ArrayNode messages = objectMapper.createArrayNode();
            for (Map<String, String> history : historyMessages) {
                messages.add(objectMapper.createObjectNode()
                        .put("role", history.get("role"))
                        .put("content", history.get("content")));
            }
            messages.add(objectMapper.createObjectNode()
                    .put("role", "user")
                    .put("content", message));
            ((com.fasterxml.jackson.databind.node.ObjectNode) root).set("messages", messages);
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("AI请求参数构造失败");
        }
    }

    private String parseAssistantContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isTextual()) {
                return contentNode.asText();
            }
            if (contentNode.isArray()) {
                StringBuilder builder = new StringBuilder();
                Iterator<JsonNode> iterator = contentNode.elements();
                while (iterator.hasNext()) {
                    JsonNode item = iterator.next();
                    if (item.has("text") && item.get("text").isTextual()) {
                        builder.append(item.get("text").asText());
                    }
                }
                return builder.toString();
            }
            return "";
        } catch (Exception e) {
            log.error("Ark AI响应解析失败: body={}", responseBody, e);
            throw new RuntimeException("AI响应解析失败");
        }
    }

    private List<Map<String, String>> loadHistoryMessages(String conversationId) {
        List<Map<String, String>> result = new ArrayList<>();
        List<BotHistoryItemVO> historyItems = parseHistoryFromRedis(conversationId);
        for (BotHistoryItemVO item : historyItems) {
            Map<String, String> map = new HashMap<>();
            map.put("role", item.getRole());
            map.put("content", item.getContent());
            result.add(map);
        }
        return result;
    }

    private List<BotHistoryItemVO> parseHistoryFromRedis(String conversationId) {
        List<BotHistoryItemVO> result = new ArrayList<>();
        String contextKey = buildContextKey(conversationId);
        List<String> rawHistory = stringRedisTemplate.opsForList().range(contextKey, 0, -1);
        if (rawHistory == null || rawHistory.isEmpty()) {
            return result;
        }
        for (String item : rawHistory) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            try {
                JsonNode node = objectMapper.readTree(item);
                String role = node.path("role").asText();
                String content = node.path("content").asText();
                if (StringUtils.isBlank(role) || StringUtils.isBlank(content)) {
                    continue;
                }
                BotHistoryItemVO historyItemVO = new BotHistoryItemVO();
                historyItemVO.setRole(role);
                historyItemVO.setContent(content);
                JsonNode timestampNode = node.path("timestamp");
                historyItemVO.setTimestamp(timestampNode.isMissingNode() ? null : timestampNode.asLong());
                result.add(historyItemVO);
            } catch (Exception e) {
                log.warn("解析会话历史失败, conversationId={}", conversationId, e);
            }
        }
        return result;
    }

    private void appendConversation(String conversationId, String role, String content) {
        String contextKey = buildContextKey(conversationId);
        try {
            String payload = objectMapper.createObjectNode()
                    .put("role", role)
                    .put("content", content)
                    .put("timestamp", System.currentTimeMillis())
                    .toString();
            stringRedisTemplate.opsForList().rightPush(contextKey, payload);
            stringRedisTemplate.expire(contextKey, arkAiProperties.getContextTtlMinutes(), TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("写入会话历史失败, conversationId={}", conversationId, e);
        }
    }

    private void trimHistory(String conversationId) {
        String contextKey = buildContextKey(conversationId);
        Long size = stringRedisTemplate.opsForList().size(contextKey);
        if (size == null) {
            return;
        }
        int maxHistory = arkAiProperties.getMaxHistoryMessages() == null ? 10 : arkAiProperties.getMaxHistoryMessages();
        if (size > maxHistory) {
            stringRedisTemplate.opsForList().trim(contextKey, size - maxHistory, size - 1);
        }
    }

    private String buildContextKey(String conversationId) {
        return CONTEXT_KEY_PREFIX + conversationId;
    }

    private void appendDrawingHistory(Long userId, String model, String prompt, String text, String imageUrl) {
        if (userId == null) {
            return;
        }
        String key = buildDrawingHistoryKey(userId);
        try {
            String payload = objectMapper.createObjectNode()
                    .put("recordId", java.util.UUID.randomUUID().toString())
                    .put("userId", userId)
                    .put("model", model)
                    .put("prompt", prompt == null ? "" : prompt)
                    .put("text", text == null ? "" : text)
                    .put("imageUrl", imageUrl == null ? "" : imageUrl)
                    .put("timestamp", System.currentTimeMillis())
                    .toString();
            stringRedisTemplate.opsForList().rightPush(key, payload);
            stringRedisTemplate.expire(key, arkAiProperties.getContextTtlMinutes(), TimeUnit.MINUTES);
            stringRedisTemplate.opsForList().trim(key, -100, -1);
        } catch (Exception e) {
            log.warn("写入绘画历史失败 userId={}", userId, e);
        }
    }

    private String buildDrawingHistoryKey(Long userId) {
        return DRAWING_HISTORY_KEY_PREFIX + userId;
    }

    private void upsertConversationSession(Long userId,
                                           String conversationId,
                                           String model,
                                           String userMessage,
                                           String assistantMessage) {
        long finalUserId = normalizeUserId(userId);
        long now = System.currentTimeMillis();
        String metaKey = buildSessionMetaKey(conversationId);
        String userSessionsKey = buildUserSessionsKey(finalUserId);
        BotConversationSessionVO existing = getSessionMeta(finalUserId, conversationId);
        String title = existing == null || StringUtils.isBlank(existing.getTitle())
                ? shorten(userMessage, 24)
                : existing.getTitle();
        String lastMessage = StringUtils.isNotBlank(assistantMessage) ? assistantMessage : userMessage;
        try {
            String payload = objectMapper.createObjectNode()
                    .put("userId", finalUserId)
                    .put("conversationId", conversationId)
                    .put("title", StringUtils.defaultString(title, "新会话"))
                    .put("lastMessage", shorten(lastMessage, 120))
                    .put("model", StringUtils.defaultString(model, ""))
                    .put("lastTimestamp", now)
                    .toString();
            stringRedisTemplate.opsForValue().set(metaKey, payload);
            stringRedisTemplate.opsForZSet().add(userSessionsKey, conversationId, now);
            stringRedisTemplate.expire(metaKey, arkAiProperties.getContextTtlMinutes(), TimeUnit.MINUTES);
            stringRedisTemplate.expire(userSessionsKey, arkAiProperties.getContextTtlMinutes(), TimeUnit.MINUTES);
            Long size = stringRedisTemplate.opsForZSet().zCard(userSessionsKey);
            if (size != null && size > 200) {
                stringRedisTemplate.opsForZSet().removeRange(userSessionsKey, 0, size - 201);
            }
        } catch (Exception e) {
            log.warn("写入会话元数据失败, conversationId={} userId={}", conversationId, finalUserId, e);
        }
    }

    private BotConversationSessionVO getSessionMeta(Long userId, String conversationId) {
        String metaKey = buildSessionMetaKey(conversationId);
        String raw = stringRedisTemplate.opsForValue().get(metaKey);
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(raw);
            Long owner = node.path("userId").isMissingNode() ? null : node.path("userId").asLong();
            long finalUserId = normalizeUserId(userId);
            if (owner != null && owner != finalUserId) {
                return null;
            }
            BotConversationSessionVO sessionVO = new BotConversationSessionVO();
            sessionVO.setUserId(owner == null ? finalUserId : owner);
            sessionVO.setConversationId(node.path("conversationId").asText(conversationId));
            sessionVO.setTitle(node.path("title").asText("新会话"));
            sessionVO.setLastMessage(node.path("lastMessage").asText(""));
            sessionVO.setModel(node.path("model").asText(""));
            sessionVO.setLastTimestamp(node.path("lastTimestamp").isMissingNode() ? null : node.path("lastTimestamp").asLong());
            return sessionVO;
        } catch (Exception e) {
            log.warn("解析会话元数据失败 conversationId={}", conversationId, e);
            return null;
        }
    }

    private String buildSessionMetaKey(String conversationId) {
        return SESSION_META_KEY_PREFIX + conversationId;
    }

    private String buildUserSessionsKey(Long userId) {
        return USER_SESSIONS_KEY_PREFIX + normalizeUserId(userId);
    }

    private long normalizeUserId(Long userId) {
        return userId == null ? 0L : userId;
    }

    private String shorten(String text, int maxLength) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        String normalized = text.replace('\n', ' ').replace('\r', ' ').trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }

    private String trimTrailingSlash(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        if (text.endsWith("/")) {
            return text.substring(0, text.length() - 1);
        }
        return text;
    }

    private void validateChatModel(String model) {
        validateModelByWhitelist(model, arkAiProperties.getAllowedChatModels());
    }

    private void validateEmbeddingModel(String model) {
        validateModelByWhitelist(model, arkAiProperties.getAllowedEmbeddingModels());
    }

    private void validateModelByWhitelist(String model, List<String> whitelist) {
        if (StringUtils.isBlank(model)) {
            throw new IllegalArgumentException("model 不能为空");
        }
        if (whitelist != null && !whitelist.isEmpty()) {
            if (!whitelist.contains(model)) {
                throw new IllegalArgumentException("模型未授权: " + model);
            }
            return;
        }
        List<String> fallback = arkAiProperties.getAllowedModels();
        if (fallback == null || fallback.isEmpty()) {
            return;
        }
        if (!fallback.contains(model)) {
            throw new IllegalArgumentException("模型未授权: " + model);
        }
    }
}
