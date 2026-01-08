package com.fzg.mapper;

import com.fzg.model.FavoriteFolder;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteFoldermapper {
    int deleteByPrimaryKey(Long id);

    int insert(FavoriteFolder record);

    int insertSelective(FavoriteFolder record);

    FavoriteFolder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FavoriteFolder record);

    int updateByPrimaryKey(FavoriteFolder record);
}