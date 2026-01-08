package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.NewsType;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yanju
* @description 针对表【news_type(新闻类型表)】的数据库操作Mapper
* @createDate 2025-07-09 17:09:29
* @Entity model.NewsType
*/
@Mapper
public interface NewsTypeMapper extends BaseMapper<NewsType> {

}




