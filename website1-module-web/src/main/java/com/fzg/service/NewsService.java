package com.fzg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.bo.NewsCreateBO;
import com.fzg.model.News;
import com.fzg.model.Result;
import com.fzg.vo.NewsVO;

import java.util.List;

/**
* @author yanju
* @description 针对表【news(新闻表)】的数据库操作Service
* @createDate 2025-07-09 17:09:29
*/
public interface NewsService extends IService<News> {


    Result createNewsWithContent(NewsCreateBO newsCreateBO);

    void deleteNews(Integer id);

    void updateNews(Integer id, NewsCreateBO newsCreateBO);

    Result<Page<NewsVO>> userNewsList(Integer pageNumber, Integer i);

    Result<List<NewsCreateBO>> adminNewsList();
}
