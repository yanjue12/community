package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.LikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {
    List<Long> queryLikedByUserBatch(@Param("userId") Long userId, @Param("articleIds") List<Long> articleIds);
}
