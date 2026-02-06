package com.fzg.service;

public interface SensitiveService {
    /**
     * 命中返回词，否则 null
     */
    String hit(String... texts);
}
