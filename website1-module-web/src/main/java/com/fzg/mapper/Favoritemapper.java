package com.fzg.mapper;

import com.fzg.model.Favorite;
import org.springframework.stereotype.Repository;

@Repository
public interface Favoritemapper {
    int deleteByPrimaryKey(Long id);

    int insert(Favorite record);

    int insertSelective(Favorite record);

    Favorite selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Favorite record);

    int updateByPrimaryKey(Favorite record);
}