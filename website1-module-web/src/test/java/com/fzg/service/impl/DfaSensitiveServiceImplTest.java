package com.fzg.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DfaSensitiveServiceImplTest {

    @Test
    void shouldHitSensitiveWord() {
        DfaSensitiveServiceImpl service = new DfaSensitiveServiceImpl();
        service.init();

        String hit = service.hit("这是一段包含色情内容的文本");

        Assertions.assertEquals("色情", hit);
    }

    @Test
    void shouldIgnoreWhitespaceWhenMatching() {
        DfaSensitiveServiceImpl service = new DfaSensitiveServiceImpl();
        service.init();

        String hit = service.hit("这段话里有色 情词语");

        Assertions.assertEquals("色情", hit);
    }

    @Test
    void shouldReturnNullForNormalText() {
        DfaSensitiveServiceImpl service = new DfaSensitiveServiceImpl();
        service.init();

        String hit = service.hit("这是一篇正常的学习交流文章");

        Assertions.assertNull(hit);
    }
}
