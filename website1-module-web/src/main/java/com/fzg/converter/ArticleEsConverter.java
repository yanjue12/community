package com.fzg.converter;

import com.fzg.mapper.ArticleTagRelationmapper;
import com.fzg.model.Article;
import com.fzg.model.ArticleEs;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
        es.setViewCount(Long.valueOf(article.getViewCount()));
        es.setLikeCount(Long.valueOf(article.getLikeCount()));

        // 时间字段：LocalDateTime
        if (article.getCreatedAt() != null) {
            es.setCreateTime(article.getCreatedAt());
        }

        return es;
    }
}
