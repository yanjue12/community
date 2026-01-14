package com.fzg.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.Articlemapper;
import com.fzg.model.Article;
import com.fzg.service.ArticleService;
import com.fzg.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<Articlemapper, Article> implements ArticleService {




    /**
     * 查首页帖子信息
     *
     * @param articleRequest
     * @return
     */
    @Override
    public ArticlePageVO queryListByArticleType(ArticleRequest articleRequest) {
        log.info("queryListByArticleType入参:{}",articleRequest);
        Integer pageNum = articleRequest.getPageNum() == null ? 1 : articleRequest.getPageNum();
        Integer pageSize = articleRequest.getPageSize() == null ? 10 : articleRequest.getPageSize();
        ArticlePageVO articlePageVO = new ArticlePageVO();
        try {
            List<ArticleVO> articleVOList = baseMapper.queryListByArticleType(articleRequest,pageSize,(pageNum-1)  * pageSize);
            Integer total = baseMapper.queryListCount(articleRequest);
            articlePageVO.setTotal(total);
            articlePageVO.setArticleVOList(articleVOList);
            log.info("查询文章数据成功,总数：{},数据：{}",total, JSON.toJSONString(articleVOList));
        }catch (Exception e){
            log.info("查询异常:{}",e);
            throw new RuntimeException(e);
        }
        return articlePageVO;
    }

    /**
     * 查询搜索提示词
     * @return
     */
    @Override
    public List<String> searchSuggestions() {
        List<String> searchSuggestions = baseMapper.searchSuggestions();
        return searchSuggestions;
    }

    @Override
    public ResultSearchVO search(ArticleRequest searchRequset) {
        String keyword = searchRequset.getType();
        Integer pageNum = searchRequset.getPageNum() == null ? 1 : searchRequset.getPageNum();
        Integer pageSize = searchRequset.getPageSize() == null ? 10 : searchRequset.getPageSize();

        //分组 三个维度 文章：标题和简介，，用户：昵称和用户名，，标签：标签名并反查文章
        ResultSearchVO resultSearchVO = new ResultSearchVO();
        //文章标题和简介
        List<ArticleVO> articlesByTitle =
                baseMapper.searchByTitle(keyword, pageSize, (pageNum-1)  * pageSize);
        resultSearchVO.setArticles(articlesByTitle);
        log.info("根据文章标题和简介查询出数据个数：{}",articlesByTitle.size());

        //文章：标签命中（反查文章）
        List<ArticleVO> articlesByTag =
                baseMapper.searchByTag(keyword, pageSize, (pageNum-1)  * pageSize);
        resultSearchVO.setArticlesByTag(articlesByTag);
        log.info("根据文章标签查询出数据个数：{}",articlesByTag.size());

        //用户昵称
        List<UserVO> users = baseMapper.searchByName(keyword, pageSize, (pageNum-1)  * pageSize);
        resultSearchVO.setUsers(users);
        log.info("根据用户昵称查询出数据个数：{}",users.size());

        return resultSearchVO;
    }
}
