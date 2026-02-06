package com.fzg.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fzg.annotation.ArticleViewTrack;
import com.fzg.constant.RedisArticleKey;
import com.fzg.mapper.*;
import com.fzg.model.*;
import com.fzg.service.ArticleService;
import com.fzg.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    private Articlemapper articlemapper;
    @Autowired
    private DraftMapper draftmapper;
    @Autowired
    private UserPrivacyMapper  userPrivacyMapper;
    @Autowired
    private Followmapper followmapper;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;



    /**
     * 撤回待审核的文章
     * @param articleRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean recallPendingArticles(ArticleRequest articleRequest) {
        if(null == articleRequest.getArticleId() || articleRequest.getUserId() == null){
            log.error("recallPendingArticles 参数校失败");
            return false;
        }

        try {
            Article article = articlemapper.selectById(articleRequest.getArticleId());
            article.setStatus("5");
            articlemapper.updateById(article);

            //添加草稿箱
            Draft draft = new Draft();
            BeanUtils.copyProperties(article,draft);
            draft.setId(null);
            log.info("添加草稿箱的实体：{}",JSON.toJSONString(draft));
            draft.setModuleType(article.getType());
            draft.setCreatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
            draft.setLastModifiedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
            draftmapper.insert(draft);
        } catch (BeansException e) {
            log.error("recallPendingArticles 撤回审核内容失败发送异常:{}",e);
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * 查询喜欢列表
     * @param userId
     * @param pageSize
     * @param offset
     */
    @Override
    public List<ArticleVO> queryArtLikeById(Long userId, Integer pageSize, Integer offset) {
        List<ArticleVO> articleVOList = baseMapper.queryArtLikeById(userId,pageSize,offset);
        //获取收藏状态
        List<Long> articleIds = articleVOList.stream().map(ArticleVO::getId).collect(Collectors.toList());

        //优先从redis获取点赞 收藏 状态
        Set<Long> favoriteArticleIds = new HashSet<>();
        for (Long articleId : articleIds) {
            String favoriteKey = RedisArticleKey.getFavoriteArticleStatusKey(userId, articleId);
            Boolean hasFavorite = redisTemplate.hasKey(favoriteKey);
            if (Boolean.TRUE.equals(hasFavorite)) {
                favoriteArticleIds.add(articleId);
            }
        }
        if(favoriteArticleIds.isEmpty()){
            List<Long> favoriteList = favoritemapper.queryFavoriteByUserBatch(userId, articleIds);
            favoriteArticleIds =
                    null == favoriteList ? Collections.emptySet() : new HashSet<>(favoriteList);
        }

        //设置点赞状态
        for (ArticleVO articleVO : articleVOList) {
            articleVO.setFavorited(favoriteArticleIds.contains(articleVO.getId()));
        }
        return articleVOList;
    }

    @Override
    public List<ArticleVO> queryFavoriteArtById(Long userId, Integer pageSize, Integer offset) {
        List<ArticleVO> articleVOList = baseMapper.queryArtFavById(userId,pageSize,offset);
        log.info("查询收藏列表大小：{}",articleVOList.size());
        if(articleVOList.isEmpty()){
            return articleVOList;
        }
        List<Long> articleIds = articleVOList.stream().map(ArticleVO::getId).collect(Collectors.toList());

        //优先从redis获取点赞 收藏 状态
        Set< Long> likedArticleIds = new HashSet<>();
        for (Long articleId : articleIds) {
            String likeKey = RedisArticleKey.getLikeArticleStatusKey(userId, articleId);
            Boolean hasLiked = redisTemplate.hasKey(likeKey);
            if (Boolean.TRUE.equals(hasLiked)) {
                likedArticleIds.add(articleId);
            }
        }
        // 如果缓存未命中，从数据库查询
        if (likedArticleIds.isEmpty()) {
            List<Long> likedList = likeRecordMapper.queryLikedByUserBatch(userId, articleIds);
            likedArticleIds =
                    null == likedList ? Collections.emptySet() : new HashSet<>(likedList);
        }
        //设置点赞状态
        for (ArticleVO articleVO : articleVOList) {
            articleVO.setLiked(likedArticleIds.contains(articleVO.getId()));
        }

        return articleVOList;
    }


    @Override
    @ArticleViewTrack
    public ArticleDetailVO queryArticleDetails(ArticleRequest articleRequest) {

        Long articleId = articleRequest.getArticleId();
        ArticleDetailVO article = baseMapper.queryArticleDetails(articleId);
        log.info("查询文章详细--------------");
        log.info("文章详细：{}",JSON.toJSONString( article));
        log.info("判断是否能评论之前--------------");
        //文章浏览量 + 1，，如果同一id短时间多次访问，浏览量只 + 1

        // 评论数：可以来自 article 表冗余字段，或者 count
        Long commentCount = commentmapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 1)
        );
        article.setCommentCount(commentCount.intValue());
        //判断是否可评论
        LambdaQueryWrapper<UserPrivacy> l = new LambdaQueryWrapper<>();
        l.eq(UserPrivacy::getUserId,article.getAuthorId());
        UserPrivacy userPrivacy = userPrivacyMapper.selectOne(l);
        String canComment = userPrivacy.getCanComment();
        if("0".equals(canComment)){
            article.setCanComment("Public");
        }else if("1".equals(canComment)){
            article.setCanComment("仅自己可评论");
        }else if("2".equals(canComment)){
            //粉丝可评论 判断是否是粉丝
            LambdaQueryWrapper<Follow> f = new LambdaQueryWrapper<>();
            f.eq(Follow::getFollowerId,articleRequest.getUserId())
                    .eq(Follow::getFollowingId,article.getAuthorId());
            Follow follow = followmapper.selectOne(f);
            if(null != follow){//是粉丝
                article.setCanComment("是粉丝可以评论");
            }else{
                article.setCanComment("非粉丝不可评论");
            }
        }else if("3".equals(canComment)){//互相关注可评论
            LambdaQueryWrapper<Follow> f = new LambdaQueryWrapper<>();
            f.eq(Follow::getFollowerId,article.getAuthorId()).eq(Follow::getFollowingId,articleRequest.getUserId());
            Follow follow = followmapper.selectOne(f);
            if(null != follow){
                f.clear();
                f.eq(Follow::getFollowerId,articleRequest.getUserId()).eq(Follow::getFollowingId,article.getAuthorId());
                Follow fo = followmapper.selectOne(f);
                if (null != fo){
                    article.setCanComment("互关可评论");
                }else{
                    article.setCanComment("非互关不可评论");
                }
            }else{
                article.setCanComment("非互关不可评论");
            }
        }

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
    public ResultSearchVO searchArticleByEs(ArticleRequest searchRequset) {

        String keyword = searchRequset.getType();
        Integer pageNum = searchRequset.getPageNum() == null ? 1 : searchRequset.getPageNum();
        Integer pageSize = searchRequset.getPageSize() == null ? 10 : searchRequset.getPageSize();
        Long userId = searchRequset.getUserId();

        ResultSearchVO result = new ResultSearchVO();
        int offset = (pageNum - 1) * pageSize;

    /* =========================
        文章：标题 + 内容（ES）
       ========================= */
        BoolQueryBuilder query = QueryBuilders.boolQuery()
                .should(QueryBuilders.multiMatchQuery(keyword, "title^5", "content"))
                .should(QueryBuilders.multiMatchQuery(keyword, "title^3", "content")
                        .fuzziness(Fuzziness.AUTO))
                .minimumShouldMatch(1);




        NativeSearchQuery articleSearch = new NativeSearchQueryBuilder()
                .withQuery(query)
                .withPageable(PageRequest.of(pageNum - 1, pageSize))
                .build();

        List<Long> articleIds = elasticsearchRestTemplate
                .search(articleSearch, ArticleEs.class)
                .stream()
                .map(hit -> hit.getContent().getId())
                .collect(Collectors.toList());

        if (!articleIds.isEmpty()) {
            List<ArticleVO> articles =
                    baseMapper.selectArticlesByIds(articleIds);
            result.setArticles(articles);
        } else {
            result.setArticles(Collections.emptyList());
        }

    /* =========================
        文章：标签命中（ES）
       ========================= */
        NativeSearchQuery tagSearch = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("tags", keyword))
                .withPageable(PageRequest.of(pageNum - 1, pageSize))
                .build();

        List<Long> tagArticleIds = elasticsearchRestTemplate
                .search(tagSearch, ArticleEs.class)
                .stream()
                .map(hit -> hit.getContent().getId())
                .collect(Collectors.toList());

        if (!tagArticleIds.isEmpty()) {
            List<ArticleVO> articlesByTag =
                    baseMapper.selectArticlesByIds(tagArticleIds);
            result.setArticlesByTag(articlesByTag);
        } else {
            result.setArticlesByTag(Collections.emptyList());
        }

    /* =========================
         用户：仍然 MySQL
       ========================= */
        List<UserVO> users =
                baseMapper.searchByName(keyword, userId, pageSize, offset);
        result.setUsers(users);

        return result;
    }


}
