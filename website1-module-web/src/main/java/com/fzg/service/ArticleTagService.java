package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.ArticleTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ArticleTagService extends IService<ArticleTag> {

    List<String> listTagNamesByArticleId(@Param("id") Long id);
}
