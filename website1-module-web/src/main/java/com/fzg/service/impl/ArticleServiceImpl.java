package com.fzg.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.DictionaryMapper;
import com.fzg.model.Article;
import com.fzg.model.Dictionary;
import com.fzg.service.ArticleService;
import com.fzg.vo.ArticleRequest;
import com.fzg.vo.ArticleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<Articlemapper, Article> implements ArticleService {


    @Autowired
    private DictionaryMapper dictionaryMapper;

    /**
     * 查首页帖子信息
     * @param articleRequest
     * @return
     */
    @Override
    public List<ArticleVO> queryListByArticleType(ArticleRequest articleRequest) {
        log.info("queryListByArticleType入参:{}",articleRequest);
        Integer pageNum = articleRequest.getPageNum() == null ? 1 : articleRequest.getPageNum();
        Integer pageSize = articleRequest.getPageSize() == null ? 10 : articleRequest.getPageSize();

        //查字典 根据菜单
        List<Dictionary> dict = dictionaryMapper.queryListByType("MENU_TYPE");
        List<Dictionary> filterDictList = dict.stream().filter(item -> item.getDictValue().equals(articleRequest.getType())).collect(Collectors.toList());
        log.info("查询字典表成功,数据：{}", JSON.toJSONString(filterDictList));
        if(CollectionUtils.isEmpty(filterDictList)){
            log.info("过滤出来为空");
            return new ArrayList<>();
        }
        // 0 热榜 1推荐 2关注 3最新
        List<ArticleVO> articleVOList = new ArrayList<>();
        try {
            articleVOList = baseMapper.queryListByArticleType(articleRequest,pageNum,pageSize);
            log.info("查询文章数据成功,数据：{}", JSON.toJSONString(articleVOList));
        }catch (Exception e){
            log.info("查询异常:{}",e);
            throw new RuntimeException(e);
        }

        return articleVOList;
    }
}
