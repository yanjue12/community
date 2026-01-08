package com.fzg.mapper;

import com.fzg.model.Article;
import org.springframework.stereotype.Repository;

@Repository
public interface Articlemapper {
    int deleteByPrimaryKey(Long id);

    int insert(Article record);

    int insertSelective(Article record);

    Article selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Article record);

    int updateByPrimaryKey(Article record);
}