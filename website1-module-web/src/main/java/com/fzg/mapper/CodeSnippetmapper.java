package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.CodeSnippet;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeSnippetmapper extends BaseMapper<CodeSnippet> {
    int deleteByPrimaryKey(Long id);

    int insert(CodeSnippet record);

    int insertSelective(CodeSnippet record);

    CodeSnippet selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CodeSnippet record);

    int updateByPrimaryKey(CodeSnippet record);
}