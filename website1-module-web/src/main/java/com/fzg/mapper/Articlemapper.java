package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Article;
import com.fzg.vo.*;
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

    List<UserVO> searchByName(@Param("keyword") String keyword,@Param("userId") Long userId, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);


    void batchUpdateLikeCount(@Param("list") List<ArticleLikeVO> batchList);

    void upArticleLikeCount(@Param("articleId") Long articleId, @Param("actionLike") Integer actionLike,@Param("type") String type);

    List<ArticleVO> queryArticleByUserId(@Param("userId") Long userId, @Param("pageSize") Integer pageSize, @Param("offset") Integer i);

    int updateBatchById(@Param("userIds") List<Long> articleUserIds);

    ArticleDetailVO queryArticleDetails(@Param("articleId") Long articleId);

    void incrViewCount(@Param("articleId") Long articleId);

    List<ArticleVO> queryArtLikeById(@Param("userId") Long userId, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);

    List<ArticleVO> queryArtPendingById(@Param("userId") Long userId, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);

    List<ArticleVO> queryArtFavById(@Param("userId") Long userId, @Param("pageSize") Integer pageSize, @Param("offset") Integer offset);
}