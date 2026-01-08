package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.SearchHot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SearchHotmapper extends BaseMapper<SearchHot> {


    List<SearchHot> queryHot();
}