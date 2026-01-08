package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Article;
import com.fzg.model.ArticleTagRelation;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleTagRelationmapper extends BaseMapper<ArticleTagRelation> {

}