package com.fzg.essync;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.converter.ArticleEsConverter;
import com.fzg.mapper.Articlemapper;
import com.fzg.model.Article;
import com.fzg.model.ArticleEs;
import com.fzg.service.ArticleTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleEsSyncService {

    @Autowired
    private Articlemapper articleMapper;
    @Autowired
    private ArticleTagService articleTagService;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 全量同步已发布文章到 ES
     */
    @Transactional(readOnly = true)
    public void fullSyncToEs() {

        // 1. 查已发布文章（status = 1）
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, "1")
        );

        if (articles.isEmpty()) {
            return;
        }

        // 2. 转成 ES 实体
        List<ArticleEs> esList = new ArrayList<>();

        for (Article article : articles) {
            List<String> tags =
                    articleTagService.listTagNamesByArticleId(article.getId());

            esList.add(ArticleEsConverter.toEs(article, tags));
        }

        // 3. 批量写入 ES
        elasticsearchRestTemplate.save(esList);
    }
}
