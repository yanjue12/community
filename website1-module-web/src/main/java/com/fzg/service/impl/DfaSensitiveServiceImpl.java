package com.fzg.service.impl;

import com.fzg.dfa.DfaNode;
import com.fzg.service.SensitiveService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Primary
public class DfaSensitiveServiceImpl implements SensitiveService {

    private final DfaNode root = new DfaNode();

    /**
     * 系统启动时初始化
     */
    @PostConstruct
    public void init() {
        // 后续可以从 DB / Redis / 文件加载
        addWord("赌博");
        addWord("黄色");
        addWord("政治");
        addWord("涉黄网站");
    }

    /**
     * 添加敏感词（构建 DFA）
     */
    public void addWord(String word) {
        DfaNode node = root;
        for (char c : word.toCharArray()) {
            node = node.getChildren()
                       .computeIfAbsent(c, k -> new DfaNode());
        }
        node.setEnd(true);
    }

    /**
     * 命中检测：返回第一个命中的敏感词
     */
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

    /**
     * DFA 核心扫描逻辑（O(n)）
     */
    private String check(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            DfaNode node = root;
            int j = i;

            while (j < chars.length) {
                node = node.getChildren().get(chars[j]);
                if (node == null) {
                    break;
                }
                if (node.isEnd()) {
                    // 命中，返回敏感词
                    return text.substring(i, j + 1);
                }
                j++;
            }
        }
        return null;
    }
}
