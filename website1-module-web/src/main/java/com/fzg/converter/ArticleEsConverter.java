package com.fzg.converter;

import com.fzg.mapper.ArticleTagRelationmapper;
import com.fzg.model.Article;
import com.fzg.model.ArticleEs;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

public final class ArticleEsConverter {


    private ArticleEsConverter() {
        // 工具类，禁止实例化
    }

    public static ArticleEs toEs(Article article, List<String> tags) {
        if (article == null) {
            return null;
        }

        ArticleEs es = new ArticleEs();
        es.setId(article.getId());
        es.setTitle(article.getTitle());
        es.setContent(article.getContent());

        // tags：注意类型兼容
        if (tags != null && !tags.isEmpty()) {
            es.setTags(tags.toArray(new String[0]));
        }

        es.setUserId(article.getUserId());
        // 使用 Optional.ofNullable 处理可能的空值
        es.setViewCount(Optional.ofNullable(article.getViewCount()).map(Long::valueOf).orElse(0L));
        es.setLikeCount(Optional.ofNullable(article.getLikeCount()).map(Long::valueOf).orElse(0L));

        // 时间字段：LocalDateTime
        if (article.getCreatedAt() != null) {
            es.setCreateTime(article.getCreatedAt());
        }

        return es;
    }
}
