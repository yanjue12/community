package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Draft;
import com.fzg.model.Result;
import com.fzg.vo.ArticleRequest;
import com.fzg.vo.DraftVO;

import java.util.List;

public interface DraftService extends IService<Draft> {
    Result saveArticleDraft(ArticleRequest articleRequest);

    List<DraftVO> queryArticleDraft(Long userId, Integer pageNum, Integer pageSize);
}
