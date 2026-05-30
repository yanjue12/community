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

    /**
     * 获取引导式注册使用的候选标签（按使用热度降序）
     * 用于冷启动场景下引导新用户选择技术标签
     */
    List<ArticleTag> queryGuideTags(@Param("limit") int limit);
}