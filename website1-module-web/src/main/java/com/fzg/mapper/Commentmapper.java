package com.fzg.mapper;

import com.fzg.model.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface Commentmapper {
    int deleteByPrimaryKey(Long id);

    int insert(Comment record);

    int insertSelective(Comment record);

    Comment selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Comment record);

    int updateByPrimaryKey(Comment record);
}