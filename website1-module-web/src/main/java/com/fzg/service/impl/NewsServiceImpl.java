package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.model.News;
import com.fzg.service.NewsService;
import com.fzg.mapper.NewsMapper;
import org.springframework.stereotype.Service;

/**
* @author yanju
* @description 针对表【news(新闻表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:29
*/
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News>
    implements NewsService{

}




