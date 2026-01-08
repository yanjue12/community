package com.fzg.mapper;

import com.fzg.model.ArticleTag;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleTagMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ArticleTag record);

    int insertSelective(ArticleTag record);

    ArticleTag selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ArticleTag record);

    int updateByPrimaryKey(ArticleTag record);
}