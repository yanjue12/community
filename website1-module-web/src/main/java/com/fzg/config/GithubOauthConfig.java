package com.fzg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "oauth.github")
@Data
public class GithubOauthConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}