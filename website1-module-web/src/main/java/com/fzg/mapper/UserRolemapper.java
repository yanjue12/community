package com.fzg.mapper;

import com.fzg.model.UserRole;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRolemapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserRole record);

    int insertSelective(UserRole record);

    UserRole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserRole record);

    int updateByPrimaryKey(UserRole record);
}