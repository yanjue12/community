package com.fzg.service.impl;

import com.fzg.dfa.DfaNode;
import com.fzg.service.SensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@Primary
@Slf4j
public class DfaSensitiveServiceImpl implements SensitiveService {

    private static final String DEFAULT_WORDS_PATH = "classpath:sensitive-words.txt";

    private final DfaNode root = new DfaNode();

    @Value("${content.audit.sensitive-words-location:" + DEFAULT_WORDS_PATH + "}")
    private Resource sensitiveWordsResource;

    @PostConstruct
    public void init() {
        int loaded = loadWordsFromResource();
        if (loaded == 0) {
            loadFallbackWords();
        }
    }

    public void addWord(String word) {
        if (StringUtils.isBlank(word)) {
            return;
        }

        String normalizedWord = normalize(word);
        if (normalizedWord.isEmpty()) {
            return;
        }

        DfaNode node = root;
        for (char c : normalizedWord.toCharArray()) {
            node = node.getChildren().computeIfAbsent(c, k -> new DfaNode());
        }
        node.setEnd(true);
    }

    @Override
    public String hit(String... texts) {
        if (texts == null) {
            return null;
        }
        for (String text : texts) {
            String hit = check(text);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    private String check(String text) {
        String normalizedText = normalize(text);
        if (normalizedText.isEmpty()) {
            return null;
        }

        char[] chars = normalizedText.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            DfaNode node = root;
            int j = i;

            while (j < chars.length) {
                node = node.getChildren().get(chars[j]);
                if (node == null) {
                    break;
                }
                if (node.isEnd()) {
                    return normalizedText.substring(i, j + 1);
                }
                j++;
            }
        }
        return null;
    }

    private int loadWordsFromResource() {
        if (sensitiveWordsResource == null || !sensitiveWordsResource.exists()) {
            log.warn("敏感词词库不存在，使用内置默认词库: {}", DEFAULT_WORDS_PATH);
            return 0;
        }

        int count = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(sensitiveWordsResource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (word.isEmpty() || word.startsWith("#")) {
                    continue;
                }
                addWord(word);
                count++;
            }
            log.info("敏感词词库加载完成，共加载 {} 个关键词", count);
            return count;
        } catch (IOException e) {
            log.error("敏感词词库加载失败，使用内置默认词库", e);
            return 0;
        }
    }

    private void loadFallbackWords() {
        String[] defaults = {
                "色情", "黄图", "黄片", "约炮", "裸聊", "成人视频", "嫖娼", "卖淫",
                "操你妈", "傻逼", "煞笔", "他妈的", "妈的", "滚你妈", "狗日的", "王八蛋", "鸡巴",
                "赌博", "博彩", "赌钱", "私彩",
                "冰毒", "海洛因", "吸毒",
                "诈骗", "刷单", "洗钱"
        };
        for (String word : defaults) {
            addWord(word);
        }
        log.info("已启用内置默认敏感词词库，共加载 {} 个关键词", defaults.length);
    }

    private String normalize(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }

        StringBuilder builder = new StringBuilder(text.length());
        for (char current : text.toLowerCase(Locale.ROOT).toCharArray()) {
            if (Character.isWhitespace(current)) {
                continue;
            }
            builder.append(current);
        }
        return builder.toString();
    }
}
