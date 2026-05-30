package com.fzg.service;

public interface SensitiveService {

    /**
     * 命中返回关键词，否则返回 null
     */
    String hit(String... texts);

    /**
     * 是否命中敏感词
     */
    default boolean containsSensitiveWord(String... texts) {
        return hit(texts) != null;
    }
}
