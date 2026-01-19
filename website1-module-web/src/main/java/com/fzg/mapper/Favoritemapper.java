package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Favorite;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Favoritemapper extends BaseMapper<Favorite> {

    List<Long> queryFavoriteByUserBatch(@Param("userId") Long userId, @Param("articleIds") List<Long> articleIds);
}