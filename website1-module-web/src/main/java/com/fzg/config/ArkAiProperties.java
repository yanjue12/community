package com.fzg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "ark.ai")
public class ArkAiProperties {
    private Boolean enabled = Boolean.TRUE;
    private Boolean platformEnabled = Boolean.FALSE;
    private String platformBaseUrl = "http://127.0.0.1:8000";
    private String platformApiKey;
    private Integer platformConnectTimeoutSeconds = 3;
    private Integer platformReadTimeoutSeconds = 60;
    private Integer platformRetryCount = 1;
    private Boolean platformAllowDegrade = Boolean.TRUE;
    private String defaultDepth = "balanced";
    private String apiKey;
    private String baseUrl = "https://ark.cn-beijing.volces.com/api/v3";
    private String model = "doubao-seed-2-0-pro-260215";
    private String embeddingModel = "doubao-embedding-vision-251215";
    private List<String> allowedModels = Arrays.asList(
            "doubao-seed-2-0-pro-260215",
            "doubao-1-5-pro-256k-250115",
            "doubao-embedding-vision-251215"
    );
    private List<String> allowedChatModels = Arrays.asList(
            "doubao-seed-2-0-pro-260215",
            "doubao-1-5-pro-256k-250115"
    );
    private List<String> allowedEmbeddingModels = Arrays.asList(
            "doubao-embedding-vision-251215"
    );
    private Integer timeoutSeconds = 30;
    private Integer maxHistoryMessages = 10;
    private Integer contextTtlMinutes = 120;
}
