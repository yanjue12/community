package com.fzg.mapper;

import com.fzg.model.ArticleTagRelation;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleTagRelationmapper {
    int deleteByPrimaryKey(Long id);

    int insert(ArticleTagRelation record);

    int insertSelective(ArticleTagRelation record);

    ArticleTagRelation selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ArticleTagRelation record);

    int updateByPrimaryKey(ArticleTagRelation record);
}