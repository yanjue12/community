package com.fzg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tencent.captcha")
public class TencentCaptchaProperties {
    private Boolean enabled = Boolean.FALSE;
    private String appId;
    private String appSecretKey;
    private String verifyUrl = "https://ssl.captcha.qq.com/ticket/verify";
    private Integer connectTimeoutSeconds = 3;
    private Integer readTimeoutSeconds = 5;
}
