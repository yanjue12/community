package com.fzg.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.annotation.ArticleViewTrack;
import com.fzg.constant.RedisArticleKey;
import com.fzg.constant.RedisRecommendKey;
import com.fzg.enums.ArticleListType;
import com.fzg.job.ArticleQueryExecutor;
import com.fzg.mapper.*;
import com.fzg.model.*;
import com.fzg.service.ArticleService;
import com.fzg.service.UserPrivacyService;
import com.fzg.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
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
    @Autowired
    private UserPrivacyService userPrivacyService;
    @Autowired
    private UserProfileMapper userProfileMapper;



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
        fillLikeAndFavoriteStatus(userId,articleVOList);
        return articleVOList;
    }

    @Override
    public List<ArticleVO> queryFavoriteArtById(Long userId, Integer pageSize, Integer offset) {
        List<ArticleVO> articleVOList = baseMapper.queryArtFavById(userId,pageSize,offset);
        log.info("查询收藏列表大小：{}",articleVOList.size());
        if(articleVOList.isEmpty()){
            return articleVOList;
        }
       fillLikeAndFavoriteStatus(userId,articleVOList);

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
    public ArticlePageVO queryListByArticleType(ArticleRequest request) {

        Long userId = request.getUserId();
        int pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<ArticleVO> list;

        switch (request.getType()) {
            case "0": // 热榜
                list = baseMapper.queryHotList(request, pageSize, offset);
                break;

            case "1": // 推荐
                list = queryRecommendList(request, pageSize, offset);
                break;

            case "2": // 关注
                list = baseMapper.queryFollowList(request, pageSize, offset);
                break;

            case "3": // 最新
            default:
                list = baseMapper.queryLatestList(request, pageSize, offset);
        }

        if (CollectionUtils.isEmpty(list)) {
            return new ArticlePageVO();
        }

        // 用户态补充（读 Redis / DB）
        fillLikeAndFavoriteStatus(userId, list);

        ArticlePageVO vo = new ArticlePageVO();
        vo.setArticleVOList(list);
        //记录曝光
        recordExpose(userId, list);

        return vo;
    }




    private List<ArticleVO> queryRecommendList(
            ArticleRequest request,
            int pageSize,
            int offset) {

        Long userId = request.getUserId();

        // 1️⃣ 未登录 → 热榜
        if (userId == null) {
            return baseMapper.queryHotList(request, pageSize, offset);
        }

        // 2️⃣ 查询用户画像
        UserProfile profile = userProfileMapper.selectByUserId(userId);

        // 3️⃣ 无画像 → 兜底
        if (profile == null) {
            return baseMapper.queryRecommendFallback(pageSize, offset);
        }

        Map<Long, Double> tagProfile = profile.getTagProfile();

        // 4️⃣ 标签画像为空 → 兜底
        if (CollectionUtils.isEmpty(tagProfile)) {
            return baseMapper.queryRecommendFallback(pageSize, offset);
        }

        // 5️⃣ 取 Top 权重标签
        List<TagWeightDTO> topTagWeights =
                getTopTagWeights(tagProfile, 5);

        if (CollectionUtils.isEmpty(topTagWeights)) {
            return baseMapper.queryRecommendFallback(pageSize, offset);
        }

        Set<Long> excludeIds = getExposedArticleIds(userId);

        return baseMapper.queryPersonalizedList(
                topTagWeights,
                excludeIds,
                pageSize,
                offset
        );

    }


    //获取标签权重
    private List<TagWeightDTO> getTopTagWeights(
            Map<Long, Double> tagProfile,
            int limit) {

        return tagProfile.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> new TagWeightDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    //曝光存储规则
    private void recordExpose(Long userId, List<ArticleVO> list) {

        if (userId == null || CollectionUtils.isEmpty(list)) {
            return;
        }

        String key = RedisRecommendKey.userExposeSet(userId);

        for (ArticleVO vo : list) {
            redisTemplate.opsForSet().add(key, vo.getId());
        }

        // 保留 7 天
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    //获取曝光集合
    private Set<Long> getExposedArticleIds(Long userId) {

        if (userId == null) {
            return Collections.emptySet();
        }

        String key = RedisRecommendKey.userExposeSet(userId);
        Set<Object> set = redisTemplate.opsForSet().members(key);

        if (CollectionUtils.isEmpty(set)) {
            return Collections.emptySet();
        }

        return set.stream()
                .map(o -> Long.valueOf(o.toString()))
                .collect(Collectors.toSet());
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
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        /* ========= P0：短语完全命中（最高优先级） ========= */
        query.should(
                QueryBuilders.matchPhraseQuery("title", keyword)
                        .boost(20f)
        );
        query.should(
                QueryBuilders.matchPhraseQuery("content", keyword)
                        .boost(5f)
        );

        /* ========= P1：multi_match（分词但相关） ========= */
        query.should(
                QueryBuilders.multiMatchQuery(keyword)
                        .field("title", 8)
                        .field("content", 2)
                        .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
        );

        /* ========= P2：普通 match（无模糊） ========= */
        query.should(
                QueryBuilders.matchQuery("title", keyword)
                        .operator(Operator.AND)   // 关键：AND
                        .boost(4f)
        );
        query.should(
                QueryBuilders.matchQuery("content", keyword)
                        .operator(Operator.AND)
                        .boost(1f)
        );

        /* ========= P3：错词兜底（弱） ========= */
        query.should(
                QueryBuilders.matchQuery("title", keyword)
                        .fuzziness(Fuzziness.ONE) // 只能 ONE
                        .boost(0.5f)
        );

        /* ========= 最少命中 ========= */
        query.minimumShouldMatch(1);




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
            fillLikeAndFavoriteStatus(userId, articles);
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
            fillLikeAndFavoriteStatus(userId, articlesByTag);
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



    /**
     * 批量填充文章点赞 & 收藏状态（Redis 优先，DB 兜底）
     */
    private void fillLikeAndFavoriteStatus(Long userId, List<ArticleVO> articleVOList) {

        if (CollectionUtils.isEmpty(articleVOList)) {
            return;
        }

        //未登录：明确给 false，而不是 null
        if (userId == null) {
            for (ArticleVO articleVO : articleVOList) {
                articleVO.setLiked(false);
                articleVO.setFavorited(false);
            }
            return;
        }


        List<Long> articleIds = articleVOList.stream()
                .map(ArticleVO::getId)
                .collect(Collectors.toList());

        /* ===== 点赞状态 ===== */
        Set<Long> likedArticleIds = new HashSet<>();
        for (Long articleId : articleIds) {
            String likeKey = RedisArticleKey.getLikeArticleStatusKey(userId, articleId);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(likeKey))) {
                likedArticleIds.add(articleId);
            }
        }

        if (likedArticleIds.isEmpty()) {
            List<Long> likedList = likeRecordMapper.queryLikedByUserBatch(userId, articleIds);
            if (likedList != null) {
                likedArticleIds.addAll(likedList);
            }
        }

        /* ===== 收藏状态 ===== */
        Set<Long> favoriteArticleIds = new HashSet<>();
        for (Long articleId : articleIds) {
            String favoriteKey = RedisArticleKey.getFavoriteArticleStatusKey(userId, articleId);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(favoriteKey))) {
                favoriteArticleIds.add(articleId);
            }
        }

        if (favoriteArticleIds.isEmpty()) {
            List<Long> favoriteList = favoritemapper.queryFavoriteByUserBatch(userId, articleIds);
            if (favoriteList != null) {
                favoriteArticleIds.addAll(favoriteList);
            }
        }

        /* ===== 回填 ===== */
        for (ArticleVO articleVO : articleVOList) {
            articleVO.setLiked(likedArticleIds.contains(articleVO.getId()));
            articleVO.setFavorited(favoriteArticleIds.contains(articleVO.getId()));
        }
    }


    /**
     * 查询用户主页数据
     * @param req
     * @param type
     * @return
     */
    @Override
    public List<ArticleVO> queryWithVisibility(ArticleRequest req, ArticleListType type) {

        Integer pageNum = req.getPageNum() == null ? 1 : req.getPageNum();
        Integer pageSize = req.getPageSize() == null ? 10 : req.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        Long userId = req.getUserId();
        Long authorId = req.getAuthorId();
        List<ArticleVO> result;

        //1.自己看自己
        if (Objects.equals(userId, authorId)) {
            result =  querySelfList(type, userId, pageSize, offset);
        }

        //2.权限判断
        if (!canView(userId, authorId)) {
            result =   Collections.emptyList();
        }

        //3.查他人的
        result =   queryOtherList(type, authorId, pageSize, offset);

        if (result == null) {
            return Collections.emptyList();
        }

        //4.点赞 收藏状态填充
        fillLikeAndFavoriteStatus(userId,result);

        return result;
    }

    private boolean canView(Long viewerId, Long authorId) {

        UserPrivacy privacy = userPrivacyService.getOne(
                new LambdaQueryWrapper<UserPrivacy>()
                        .eq(UserPrivacy::getUserId, authorId)
        );

        if (privacy == null) {
            return false;
        }

        switch (privacy.getArticleVisibility()) {
            case "0": // 公开
                return true;
            case "1": // 私密
                return false;
            case "2": // 粉丝可见
                return isFollower(viewerId, authorId);
            case "3": // 互关
                return isMutualFollow(viewerId, authorId);
            default:
                return false;
        }
    }

    private List<ArticleVO> querySelfList(
            ArticleListType type, Long userId, Integer pageSize, Integer offset) {

        switch (type) {
            case PUBLISH:
                return articlemapper.querySelfArticleByUserId(userId, pageSize, offset);
            case LIKE:
                return articlemapper.queryArtLikeById(userId, pageSize, offset);
            case FAVORITE:
                return this.queryFavoriteArtById(userId, pageSize, offset);
            default:
                return Collections.emptyList();
        }
    }

    private List<ArticleVO> queryOtherList(
            ArticleListType type, Long authorId, Integer pageSize, Integer offset) {

        switch (type) {
            case PUBLISH:
                return articlemapper.queryArticleByUserId(authorId, pageSize, offset);
            case LIKE:
                return articlemapper.queryArtLikeById(authorId, pageSize, offset);
            case FAVORITE:
                return this.queryFavoriteArtById(authorId, pageSize, offset);
            default:
                return Collections.emptyList();
        }
    }


    private boolean isFollower(Long userId, Long authorId) {
        return followmapper.selectOne(
                new LambdaQueryWrapper<Follow>()
                        .eq(Follow::getFollowerId, userId)
                        .eq(Follow::getFollowingId, authorId)
        ) != null;
    }

    private boolean isMutualFollow(Long userId, Long authorId) {
        return isFollower(userId, authorId)
                && isFollower(authorId, userId);
    }



}
