package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Article;
import com.fzg.vo.ArticleRequest;
import com.fzg.vo.ArticleVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Articlemapper extends BaseMapper<Article> {


    List<ArticleVO> queryListByArticleType(@Param("request") ArticleRequest articleRequest, @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);
}