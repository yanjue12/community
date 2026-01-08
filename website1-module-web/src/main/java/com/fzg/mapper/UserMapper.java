package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author yanju
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2025-07-09 17:09:29
* @Entity model.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<User> selectByCondition(@Param("condition") String condition);
}




