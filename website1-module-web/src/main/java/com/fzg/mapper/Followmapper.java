package com.fzg.mapper;

import com.fzg.model.Follow;
import org.springframework.stereotype.Repository;

@Repository
public interface Followmapper {
    int deleteByPrimaryKey(Long id);

    int insert(Follow record);

    int insertSelective(Follow record);

    Follow selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Follow record);

    int updateByPrimaryKey(Follow record);
}