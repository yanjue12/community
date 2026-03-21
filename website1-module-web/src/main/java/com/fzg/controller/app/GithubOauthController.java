package com.fzg.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.fzg.config.GithubOauthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/oauth/github")
@RequiredArgsConstructor
public class GithubOauthController {

    private final GithubOauthConfig config;

    /**
     * 跳转 GitHub 登录
     */
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        String url = "https://github.com/login/oauth/authorize" +
                "?client_id=" + config.getClientId() +
                "&redirect_uri=" + config.getRedirectUri() +
                "&scope=user" +
                "&state=" + UUID.randomUUID();

        response.sendRedirect(url);
    }


    /**
     * 绑定 GitHub（必须登录）
     */
    @GetMapping("/bind")
    public void bind(HttpServletResponse response) throws IOException {

        // 必须登录
        Long userId = StpUtil.getLoginIdAsLong();

        String state = "bind_" + userId + "_" + UUID.randomUUID();

        String url = buildGithubUrl(state);
        response.sendRedirect(url);
    }

    private String buildGithubUrl(String state) {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + config.getClientId() +
                "&redirect_uri=" + config.getRedirectUri() +
                "&scope=user" +
                "&state=" + state;
    }

}