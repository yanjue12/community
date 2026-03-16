package com.fzg.vo;

import cn.dev33.satoken.stp.SaTokenInfo;
import lombok.Data;

@Data
public class LoginResponseVO {
    private SaTokenInfo tokenInfo;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String roles;
    private String permissions;
    private String primaryRole;
}
