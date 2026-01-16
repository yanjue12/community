package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Article;
import com.fzg.vo.ArticleLikeVO;
import com.fzg.vo.ArticleRequest;
import com.fzg.vo.ArticleVO;
import com.fzg.vo.UserVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface Articlemapper extends BaseMapper<Article> {


    List<ArticleVO> queryListByArticleType(@Param("request") ArticleRequest articleRequest, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);

    Integer queryListCount(@Param("request") ArticleRequest articleRequest);

    List<String> searchSuggestions();

    List<ArticleVO> searchByTitle(@Param("keyword") String keyword, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);

    List<ArticleVO> searchByTag(@Param("keyword") String keyword, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);

    List<UserVO> searchByName(@Param("keyword") String keyword, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);


    void batchUpdateLikeCount(@Param("list") List<ArticleLikeVO> batchList);

    void upArticleLikeCount(@Param("articleId") Long articleId, @Param("actionLike") Integer actionLike);
}