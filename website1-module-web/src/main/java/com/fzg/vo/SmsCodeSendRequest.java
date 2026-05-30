package com.fzg.vo;

import lombok.Data;

@Data
public class SmsCodeSendRequest {
    private String phoneNumber;
    private String captchaTicket;
    private String captchaRandStr;
}
