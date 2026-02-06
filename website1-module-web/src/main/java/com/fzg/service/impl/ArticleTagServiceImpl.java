package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.ArticleTagMapper;
import com.fzg.model.ArticleTag;
import com.fzg.service.ArticleTagService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements ArticleTagService {


    @Override
    public List<String> listTagNamesByArticleId(Long id) {
        return baseMapper.listTagNamesByArticleId(id);
    }
}
