package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Follow;
import org.springframework.stereotype.Repository;

@Repository
public interface Followmapper extends BaseMapper<Follow> {
    int deleteByPrimaryKey(Long id);

    int insert(Follow record);

    int insertSelective(Follow record);

    Follow selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Follow record);

    int updateByPrimaryKey(Follow record);
}