package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.FavoriteFolder;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteFoldermapper extends BaseMapper<FavoriteFolder> {
    int deleteByPrimaryKey(Long id);

    int insert(FavoriteFolder record);

    int insertSelective(FavoriteFolder record);

    FavoriteFolder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FavoriteFolder record);

    int updateByPrimaryKey(FavoriteFolder record);
}