package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Article;
import com.fzg.vo.ArticleRequest;
import com.fzg.vo.ArticleVO;

import java.util.List;

public interface ArticleService extends IService<Article> {
    /**
     * 查首页帖子信息
     * @param articleRequest
     * @return
     */
    List<ArticleVO> queryListByArticleType(ArticleRequest articleRequest);
}
