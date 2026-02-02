package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Follow;
import com.fzg.model.User;
import com.fzg.vo.UserVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Followmapper extends BaseMapper<Follow> {

    List<UserVO> queryFolList(@Param("followerId") Long followerId, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);
}