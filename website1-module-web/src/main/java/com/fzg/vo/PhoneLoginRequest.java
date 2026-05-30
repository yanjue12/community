package com.fzg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PhoneLoginRequest {
    private String phoneNumber;
    private String outId;
    private String bizId;
    private String code;

    @Schema(description = "记住我 默认为false")
    private Boolean rememberMe = false;
}
