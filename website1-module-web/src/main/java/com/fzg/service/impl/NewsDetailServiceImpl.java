package com.fzg.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.NewsDetailMapper;
import com.fzg.model.NewsDetail;
import com.fzg.model.Result;
import com.fzg.service.NewsDetailService;
import com.fzg.vo.NewsDetailsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsDetailServiceImpl extends ServiceImpl<NewsDetailMapper, NewsDetail> implements NewsDetailService {

    private final NewsDetailMapper newsDetailMapper;

    @Override
    public Result<NewsDetailsVO> selectByNewsId(Integer id) {

        LambdaQueryWrapper<NewsDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NewsDetail::getNewsId, id);

        //db设计新闻和详情 一对一关系，所以可以直接查询单体对象
        NewsDetail newsDetails = newsDetailMapper.selectOne(queryWrapper);
        if(newsDetails == null){
            return Result.fail(EnumReturn.NEWS_DETAIL_NOT_FOUND);
        }
        NewsDetailsVO newsDetailsVO = new NewsDetailsVO();
        newsDetailsVO.setContent(newsDetails.getContent());

        return Result.success(newsDetailsVO);
    }
}
