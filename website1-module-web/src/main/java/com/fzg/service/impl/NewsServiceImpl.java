package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.enums.EnumReturn;
import com.fzg.model.News;
import com.fzg.model.Result;
import com.fzg.service.NewsService;
import com.fzg.mapper.NewsMapper;
import com.fzg.vo.NewsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
* @author yanju
* @description 针对表【news(新闻表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:29
*/
@Service
@Slf4j
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News>
    implements NewsService{

    @Resource
    private NewsMapper newsMapper;

    @Override
    public Result<List<NewsVO>> myList() {

        //状态正常
        LambdaQueryWrapper<News> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(News::getStates, 1);
        List<News> newsList = newsMapper.selectList(queryWrapper);

        log.info("新闻列表的数量list:{}", newsList.size());
        if(newsList.isEmpty()){
            return Result.fail(EnumReturn.NEWS_NOT_EXIST);
        }
        List<NewsVO> newsVOList = new ArrayList<>();
        for(News news: newsList){
            NewsVO newsVO = new NewsVO();
            //fixme 这里直接copy可能会出现问题导致数据丢失，需要手动赋值
            BeanUtils.copyProperties(news,newsVO);
            newsVOList.add(newsVO);
        }
        log.info("新闻列表vo数据newsVOList:{}", newsVOList);
        return Result.success(newsVOList);
    }
}




