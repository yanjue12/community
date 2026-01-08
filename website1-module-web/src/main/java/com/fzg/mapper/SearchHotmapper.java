package com.fzg.mapper;

import com.fzg.model.SearchHot;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchHotmapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SearchHot record);

    int insertSelective(SearchHot record);

    SearchHot selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SearchHot record);

    int updateByPrimaryKey(SearchHot record);
}