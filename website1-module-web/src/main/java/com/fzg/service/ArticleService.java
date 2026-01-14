package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Article;
import com.fzg.vo.ArticlePageVO;
import com.fzg.vo.ArticleRequest;
import com.fzg.vo.ResultSearchVO;

import java.util.List;


public interface ArticleService extends IService<Article> {
    /**
     * 查首页帖子信息
     * @param articleRequest
     * @return
     */
    ArticlePageVO queryListByArticleType(ArticleRequest articleRequest);

    /**
     * 查询搜索框内的搜索提示词
     * @return
     */
    List<String> searchSuggestions();

    ResultSearchVO search(ArticleRequest searchRequset);
}
