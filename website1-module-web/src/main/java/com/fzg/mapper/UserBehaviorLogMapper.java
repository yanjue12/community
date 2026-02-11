package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.UserBehaviorLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserBehaviorLogMapper extends BaseMapper<UserBehaviorLog> {

    List<UserBehaviorLog> selectRecentByUser(
            @Param("userId") Long userId,
            @Param("days") Integer days
    );


    List<UserBehaviorLog> selectRecentBehavior(@Param("userId") Long userId);
}
