package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Draft;
import com.fzg.model.Result;
import com.fzg.vo.ArticleRequest;

public interface DraftService extends IService<Draft> {
    Result saveArticleDraft(ArticleRequest articleRequest);
}
