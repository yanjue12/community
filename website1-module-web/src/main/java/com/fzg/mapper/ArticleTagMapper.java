package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.ArticleTag;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    List<ArticleTag> queryHotTags();
}