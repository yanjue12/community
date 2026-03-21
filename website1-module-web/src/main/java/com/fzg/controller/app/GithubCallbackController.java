package com.fzg.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fzg.config.GithubOauthConfig;
import com.fzg.mapper.UserMapper;
import com.fzg.mapper.UserOauthMapper;
import com.fzg.model.User;
import com.fzg.model.UserOauth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth/github")
@RequiredArgsConstructor
public class GithubCallbackController {

    private final GithubOauthConfig config;
    private final UserMapper userMapper;
    private final UserOauthMapper oauthMapper;

    @GetMapping("/callback")
    public void callback(String code, String state, HttpServletResponse response) throws IOException {

        // 1️⃣ 获取 access_token
        String accessToken = getAccessToken(code);

        // 2️⃣ 获取 GitHub 用户信息
        JSONObject githubUser = getGithubUser(accessToken);

        String githubId = githubUser.getStr("id");
        String nickname = githubUser.getStr("login");
        String avatar = githubUser.getStr("avatar_url");

        // 3️⃣ 判断是 登录 还是 绑定
        if (state.startsWith("bind_")) {
            handleBind(state, githubId, accessToken, response);
        } else {
            handleLogin(githubId, nickname, avatar, accessToken, response);
        }


    }

    private void handleLogin(String githubId, String nickname, String avatar,
                             String accessToken, HttpServletResponse response) throws IOException {

        UserOauth oauth = oauthMapper.selectByOpenId("github", githubId);

        Long userId;

        if (oauth != null) {
            userId = oauth.getUserId();
        } else {
            // 注册
            User user = new User();
            user.setNickname(nickname);
            user.setAvatar(avatar);
            userMapper.insert(user);

            UserOauth newOauth = new UserOauth();
            newOauth.setUserId(user.getId());
            newOauth.setOauthType("github");
            newOauth.setOpenId(githubId);
            newOauth.setAccessToken(accessToken);

            oauthMapper.insert(newOauth);

            userId = user.getId();
        }

        StpUtil.login(userId);
        String token = StpUtil.getTokenValue();

        response.sendRedirect("http://localhost:5173/login-success?token=" + token);
    }

    private void handleBind(String state, String githubId, String accessToken,
                            HttpServletResponse response) throws IOException {

        // 解析 userId
        String[] arr = state.split("_");
        Long userId = Long.valueOf(arr[1]);

        // ⚠️ 校验当前登录用户
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!userId.equals(currentUserId)) {
            throw new RuntimeException("非法绑定请求");
        }

        // 1️⃣ 判断该 GitHub 是否已被绑定
        UserOauth exist = oauthMapper.selectByOpenId("github", githubId);
        if (exist != null) {
            response.sendRedirect("http://localhost:5173/bind-result?msg=该GitHub已绑定其他账号");
            return;
        }

        // 2️⃣ 绑定
        UserOauth oauth = new UserOauth();
        oauth.setUserId(userId);
        oauth.setOauthType("github");
        oauth.setOpenId(githubId);
        oauth.setAccessToken(accessToken);

        oauthMapper.insert(oauth);

        response.sendRedirect("http://localhost:5173/bind-result?msg=绑定成功");
    }


    private String getAccessToken(String code) {
        String url = "https://github.com/login/oauth/access_token";

        Map<String, Object> param = new HashMap<>();
        param.put("client_id", config.getClientId());
        param.put("client_secret", config.getClientSecret());
        param.put("code", code);

        String resp = HttpUtil.post(url, param);

        return resp.split("&")[0].split("=")[1];
    }

    private JSONObject getGithubUser(String accessToken) {
        String userInfo = HttpUtil.createGet("https://api.github.com/user")
                .header("Authorization", "token " + accessToken)
                .execute()
                .body();

        return JSONUtil.parseObj(userInfo);
    }

}