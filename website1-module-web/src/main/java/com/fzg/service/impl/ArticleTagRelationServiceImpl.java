package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.ArticleTagRelationmapper;
import com.fzg.model.ArticleTagRelation;
import com.fzg.service.ArticleTagRelationService;
import org.springframework.stereotype.Service;

@Service
public class ArticleTagRelationServiceImpl extends ServiceImpl<ArticleTagRelationmapper, ArticleTagRelation> implements ArticleTagRelationService {
}
