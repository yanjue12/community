package com.fzg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsProperties {
    private Boolean enabled = Boolean.FALSE;
    private String region = "cn-beijing";
    private String endpoint = "dypnsapi.aliyuncs.com";
    private String countryCode = "86";
    private String schemeName;
    private String signName;
    private String templateCode;
    private Long validTime = 300L;
    private Long codeLength = 6L;
    private Long interval = 60L;
    private Long duplicatePolicy = 1L;
    private Long codeType = 1L;
    private Boolean returnVerifyCode = Boolean.FALSE;
}
