package com.fzg.mapper;

import com.fzg.model.Category;
import org.springframework.stereotype.Repository;

@Repository
public interface Categorymapper {
    int deleteByPrimaryKey(Long id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);
}