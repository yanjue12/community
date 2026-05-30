package com.fzg.vo;

import lombok.Data;

@Data
public class SmsCodeVerifyRequest {
    private String phoneNumber;
    private String outId;
    private String bizId;
    private String code;
}
