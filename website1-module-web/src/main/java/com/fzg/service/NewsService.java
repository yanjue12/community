package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
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

    Result<List<NewsVO>> myList();
}
