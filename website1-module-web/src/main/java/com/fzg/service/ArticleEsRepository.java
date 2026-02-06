package com.fzg.service;

import com.fzg.model.ArticleEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ArticleEsRepository
        extends ElasticsearchRepository<ArticleEs, Long> {
}
