package com.fzg.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<User> selectByCondition(@Param("condition") String condition);
}
