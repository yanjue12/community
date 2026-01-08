package com.fzg.mapper;

import com.fzg.model.Role;
import org.springframework.stereotype.Repository;

@Repository
public interface Rolemapper {
    int deleteByPrimaryKey(Long id);

    int insert(Role record);

    int insertSelective(Role record);

    Role selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Role record);

    int updateByPrimaryKey(Role record);
}