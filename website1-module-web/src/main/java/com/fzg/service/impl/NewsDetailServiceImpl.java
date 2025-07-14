package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.NewsDetailMapper;
import com.fzg.model.NewsDetail;
import com.fzg.service.NewsDetailService;
import org.springframework.stereotype.Service;

@Service
public class NewsDetailServiceImpl extends ServiceImpl<NewsDetailMapper, NewsDetail> implements NewsDetailService {
}
