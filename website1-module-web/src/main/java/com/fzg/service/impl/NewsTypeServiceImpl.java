package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.model.NewsType;
import com.fzg.service.NewsTypeService;
import com.fzg.mapper.NewsTypeMapper;
import org.springframework.stereotype.Service;

/**
* @author yanju
* @description 针对表【news_type(新闻类型表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:29
*/
@Service
public class NewsTypeServiceImpl extends ServiceImpl<NewsTypeMapper, NewsType>
    implements NewsTypeService{

}




