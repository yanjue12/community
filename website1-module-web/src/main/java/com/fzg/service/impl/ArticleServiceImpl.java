package com.fzg.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.constant.RedisArticleKey;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.Commentmapper;
import com.fzg.mapper.Favoritemapper;
import com.fzg.mapper.LikeRecordMapper;
import com.fzg.model.Article;
import com.fzg.model.Comment;
import com.fzg.service.ArticleService;
import com.fzg.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<Articlemapper, Article> implements ArticleService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private LikeRecordMapper likeRecordMapper;
    @Autowired
    private Favoritemapper favoritemapper;
    @Autowired
    private Commentmapper commentmapper;




    @Override
    public ArticleDetailVO queryArticleDetails(Long articleId) {

        ArticleDetailVO article = baseMapper.queryArticleDetails(articleId);
        LambdaQueryWrapper<Comment> q = new LambdaQueryWrapper<>();
        q.eq(Comment::getArticleId,articleId);
        List<Comment> comments = commentmapper.selectList(q);
        List<CommentVO> commentVOList = new ArrayList<>();
        for (Comment comment : comments) {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment,commentVO);
            commentVOList.add(commentVO);
        }

        article.setCommnet(commentVOList);

        return article;
    }










    /**
     * 查首页帖子信息
     *
     * @param
     * @return
     */
    @Override
    public ArticlePageVO queryListByArticleType(ArticleRequest articleRequest) {
        log.info("queryListByArticleType入参:{}",articleRequest);
        Long userId = articleRequest.getUserId();
        Integer pageNum = articleRequest.getPageNum() == null ? 1 : articleRequest.getPageNum();
        Integer pageSize = articleRequest.getPageSize() == null ? 10 : articleRequest.getPageSize();
        ArticlePageVO articlePageVO = new ArticlePageVO();
        try {
            List<ArticleVO> articleVOList = baseMapper.queryListByArticleType(articleRequest,pageSize,(pageNum-1)  * pageSize);
            //判断当前用户有没有对这些文章进行点赞 批量
            if(CollectionUtils.isEmpty(articleVOList)){
                log.error("首页查询出数据为空");
                return new ArticlePageVO();
            }
            List<Long> articleIds = articleVOList.stream().map(ArticleVO::getId).collect(Collectors.toList());

            //优先从redis获取点赞 收藏 状态
            Set< Long> likedArticleIds = new HashSet<>();
            Set<Long> favoriteArticleIds = new HashSet<>();
            for (Long articleId : articleIds) {
                String likeKey = RedisArticleKey.getLikeArticleStatusKey(userId, articleId);
                Boolean hasLiked = redisTemplate.hasKey(likeKey);
                if (Boolean.TRUE.equals(hasLiked)) {
                    likedArticleIds.add(articleId);
                }
                String favoriteKey = RedisArticleKey.getFavoriteArticleStatusKey(userId, articleId);
                Boolean hasFavorite = redisTemplate.hasKey(favoriteKey);
                if (Boolean.TRUE.equals(hasFavorite)) {
                    favoriteArticleIds.add(articleId);
                }
            }
            // 如果缓存未命中，从数据库查询
            if (likedArticleIds.isEmpty()) {
                List<Long> likedList = likeRecordMapper.queryLikedByUserBatch(userId, articleIds);
                likedArticleIds =
                    null == likedList ? Collections.emptySet() : new HashSet<>(likedList);
            }
            if(favoriteArticleIds.isEmpty()){
                List<Long> favoriteList = favoritemapper.queryFavoriteByUserBatch(userId, articleIds);
                favoriteArticleIds =
                    null == favoriteList ? Collections.emptySet() : new HashSet<>(favoriteList);
            }
            log.info("likedArticleIds:{}",likedArticleIds);

            //设置点赞状态
            for (ArticleVO articleVO : articleVOList) {
                articleVO.setLiked(likedArticleIds.contains(articleVO.getId()));
                articleVO.setFavorited(favoriteArticleIds.contains(articleVO.getId()));
            }


            articlePageVO.setArticleVOList(articleVOList);
        }catch (Exception e){
            log.info("查询异常:{}",e);
            throw new RuntimeException(e);
        }
        log.info("articlePageVO:{}", JSON.toJSON(articlePageVO));
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
        Long userId = searchRequset.getUserId();
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
        List<UserVO> users = baseMapper.searchByName(keyword,userId, pageSize, (pageNum-1)  * pageSize);
        resultSearchVO.setUsers(users);
        log.info("根据用户昵称查询出数据个数：{}",users.size());

        return resultSearchVO;
    }

}
