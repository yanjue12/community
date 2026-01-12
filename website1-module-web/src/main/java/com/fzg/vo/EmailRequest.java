package com.fzg.vo;

import lombok.Data;

@Data
public class EmailRequest {
    private String email;
    private String purpose; // 目的: "changeEmail" 或 "changePassword"
    private String code;
}
