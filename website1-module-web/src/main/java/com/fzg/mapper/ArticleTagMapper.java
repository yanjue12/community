package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.ArticleTag;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    List<ArticleTag> queryHotTags();

    Map<String, Object> queryTagStats();

    List<String> listTagNamesByArticleId(@Param("id") Long id);
}