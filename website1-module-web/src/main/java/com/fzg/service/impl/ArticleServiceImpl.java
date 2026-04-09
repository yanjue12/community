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
import com.fzg.service.UserProfileCalculateService;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<Articlemapper, Article> implements ArticleService {

    private static final int RECOMMEND_POOL_SIZE = 100;
    private static final double CF_RECALL_WEIGHT = 0.50;
    private static final double CONTENT_RECALL_WEIGHT = 0.35;
    private static final double HOT_RECALL_WEIGHT = 0.15;
    private static final int MAX_FUSION_EVAL_SIZE = 200;
    // 行为权重（用于构建隐式反馈向量）
    private static final double VIEW_BEHAVIOR_WEIGHT = 1.0;
    private static final double LIKE_BEHAVIOR_WEIGHT = 3.0;
    private static final double FAVORITE_BEHAVIOR_WEIGHT = 4.0;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
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
    @Autowired
    private UserProfileCalculateService userProfileCalculateService;
    @Autowired
    private SearchHistoryMapper searchHistoryMapper;
    @Autowired
    private ArticleViewHistoryMapper articleViewHistoryMapper;



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
        if (userPrivacy == null) {
            article.setCanComment("Public");
            return article;
        }
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









    /*  ============================================================================================
        ============================================================================================
                                 查询首页文章信息(热榜，个性化推荐，关注，最新)
        ============================================================================================
        ============================================================================================*/


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

        List<ArticleVO> list = queryHomeListByType(request, pageSize, offset, pageNum);

        if (CollectionUtils.isEmpty(list)) {
            return new ArticlePageVO();
        }

        // 用户态补充（读 Redis / DB）
        fillLikeAndFavoriteStatus(userId, list);

        ArticlePageVO vo = new ArticlePageVO();
        vo.setArticleVOList(list);
        //记录曝光 未登录暂时不记录
        if(userId != null && userId > 0){
            recordExpose(userId, list);
        }
        return vo;
    }

    // 首页四个tab统一分发：热榜/推荐/关注/最新
    private List<ArticleVO> queryHomeListByType(
            ArticleRequest request,
            int pageSize,
            int offset,
            int pageNum) {

        switch (request.getType()) {
            case "0":
                return baseMapper.queryHotList(request, pageSize, offset);
            case "1":
                return queryRecommendList(request, pageSize, offset, pageNum);
            case "2":
                return baseMapper.queryFollowList(request, pageSize, offset);
            case "3":
            default:
                return baseMapper.queryLatestList(request, pageSize, offset);
        }
    }




    private List<ArticleVO> queryRecommendList(
            ArticleRequest request,
            int pageSize,
            int offset,
            int pageNum) {

        Long userId = request.getUserId();

        if (userId == null) {
            return baseMapper.queryHotList(request, pageSize, offset);
        }

        String cacheKey = RedisRecommendKey.userRecommendList(userId);

        // ==============================
        // 1.先查缓存
        // ==============================
        List<Long> cachedIds = getCachedRecommendIds(cacheKey);

        if (CollectionUtils.isEmpty(cachedIds)) {

            // 2.没缓存 → 生成完整推荐池
            cachedIds = buildFullRecommendPool(userId);

            if (!CollectionUtils.isEmpty(cachedIds)) {
                List<String> stringIds = cachedIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                redisTemplate.opsForList().rightPushAll(cacheKey, stringIds);
                redisTemplate.expire(cacheKey, 30, TimeUnit.MINUTES);
            }
        }

        // ==============================
        // 3.切片分页
        // ==============================

        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize;

        if (start >= cachedIds.size()) {
            return Collections.emptyList();
        }

        List<Long> pageIds =
                cachedIds.subList(start, Math.min(end, cachedIds.size()));

        return baseMapper.queryByIdsPreserveOrder(pageIds);
    }

    //构建完整推荐池
    private List<Long> buildFullRecommendPool(Long userId) {

        Map<String, List<Long>> recallMap = buildMultiRecallPool(userId, RECOMMEND_POOL_SIZE);
        List<Long> hybridIds = rankByWeightedFusion(userId, recallMap, RECOMMEND_POOL_SIZE);
        if (!CollectionUtils.isEmpty(hybridIds)) {
            return hybridIds;
        }

        return baseMapper.queryHotIdsLimit(RECOMMEND_POOL_SIZE);
    }

    // 多路召回：CF + 内容兴趣 + 热榜
    private Map<String, List<Long>> buildMultiRecallPool(Long userId, int totalSize) {
        Map<String, List<Long>> recallMap = new LinkedHashMap<>();

        List<Long> cfRecall = buildCollaborativeFilteringRecommendPool(userId, totalSize);
        List<Long> contentRecall = buildContentBasedRecommendPool(userId, totalSize);
        List<Long> hotRecall = baseMapper.queryHotIdsLimit(totalSize);

        recallMap.put("cf", cfRecall);
        recallMap.put("content", contentRecall);
        recallMap.put("hot", hotRecall);

        recordRecallEval(userId, recallMap);
        return recallMap;
    }

    // 排序融合：Weighted RRF
    private List<Long> rankByWeightedFusion(
            Long userId,
            Map<String, List<Long>> recallMap,
            int totalSize) {

        if (recallMap == null || recallMap.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Double> weightMap = new HashMap<>();
        weightMap.put("cf", CF_RECALL_WEIGHT);
        weightMap.put("content", CONTENT_RECALL_WEIGHT);
        weightMap.put("hot", HOT_RECALL_WEIGHT);

        Set<Long> interactedIds = loadUserInteractionArticleIds(userId);
        Set<Long> exposedIds = getExposedArticleIds(userId);
        Set<Long> excludeIds = new HashSet<>();
        excludeIds.addAll(interactedIds);
        excludeIds.addAll(exposedIds);

        Map<Long, Double> fusionScoreMap = new HashMap<>();

        for (Map.Entry<String, List<Long>> entry : recallMap.entrySet()) {
            String channel = entry.getKey();
            List<Long> ids = entry.getValue();
            if (CollectionUtils.isEmpty(ids)) {
                continue;
            }

            double weight = weightMap.getOrDefault(channel, 0.1);
            for (int i = 0; i < ids.size(); i++) {
                Long articleId = ids.get(i);
                if (articleId == null || excludeIds.contains(articleId)) {
                    continue;
                }
                double rankScore = weight / (i + 1.0);
                fusionScoreMap.merge(articleId, rankScore, Double::sum);
            }
        }

        List<Long> result = fusionScoreMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(totalSize)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (result.size() < totalSize) {
            Set<Long> fillExcludeIds = new HashSet<>(excludeIds);
            fillExcludeIds.addAll(result);
            List<Long> hotFill = baseMapper.queryHotIdsLimit(totalSize * 2);
            for (Long id : hotFill) {
                if (!fillExcludeIds.contains(id)) {
                    result.add(id);
                    if (result.size() >= totalSize) {
                        break;
                    }
                }
            }
        }

        recordFusionEval(userId, fusionScoreMap);
        return result;
    }

    // 内容召回：复用原有画像加权逻辑，保持“冷热+探索”混排
    private List<Long> buildContentBasedRecommendPool(Long userId, int totalSize) {

        UserProfile profile = userProfileMapper.selectByUserId(userId);

        if (profile == null || profile.getProfileLevel() < 1) {
            try {
                userProfileCalculateService.calculateByUserId(userId);
                profile = userProfileMapper.selectByUserId(userId);
            } catch (Exception e) {
                log.warn("实时重算用户画像失败, userId={}, err={}", userId, e.getMessage());
            }
        }

        if (profile == null || profile.getProfileLevel() < 1) {
            return baseMapper.queryHotIdsLimit(totalSize);
        }

        int exploreSize = (int) Math.ceil(totalSize * 0.2);
        int remainSize = totalSize - exploreSize;

        int coldSize = remainSize / 2;
        int hotSize = remainSize - coldSize;

        List<TagWeightDTO> topTagWeights =
                getTopTagWeightsWithDecay(
                        profile.getTagProfile(),
                        profile.getTagLastTimeMap(),
                        10
                );

        Map<Long, Double> tagWeightMap =
                buildTagWeightMap(topTagWeights);


        List<Long> topTagIds = topTagWeights.stream()
                .map(TagWeightDTO::getTagId)
                .collect(Collectors.toList());

        Set<Long> excludeIds = new HashSet<>();

        List<Long> coldIds =
                baseMapper.queryPersonalizedListV2(
                        tagWeightMap,
                        excludeIds,
                        "cold",
                        coldSize
                );

        excludeIds.addAll(coldIds);

        List<Long> hotIds =
                baseMapper.queryPersonalizedListV2(
                        tagWeightMap,
                        excludeIds,
                        "hot",
                        hotSize
                );

        excludeIds.addAll(hotIds);
        Set<Long> exposedIds = getExposedArticleIds(userId);
        excludeIds.addAll(exposedIds);

        List<Long> exploreIds =
                baseMapper.queryExploreList(
                        excludeIds,
                        topTagIds,
                        exploreSize
                );

        List<Long> finalIds = new ArrayList<>();
        finalIds.addAll(coldIds);
        finalIds.addAll(hotIds);
        Collections.shuffle(finalIds);

        return mixIdList(finalIds, exploreIds);
    }

    private Map<Long, Double> normalize(Map<Long, Double> map) {

        double sum = map.values().stream().mapToDouble(Double::doubleValue).sum();

        if (sum == 0) return map;

        Map<Long, Double> result = new HashMap<>();

        for (Map.Entry<Long, Double> e : map.entrySet()) {
            result.put(e.getKey(), e.getValue() / sum);
        }

        return result;
    }


    private Map<Long, Double> buildTagWeightMap(List<TagWeightDTO> list) {

        Map<Long, Double> map = new HashMap<>();

        for (TagWeightDTO dto : list) {
            map.put(dto.getTagId(), dto.getWeight());
        }

        return map;
    }


    // 评估闭环-召回层：记录各路召回规模，便于离线分析召回贡献
    private void recordRecallEval(Long userId, Map<String, List<Long>> recallMap) {
        try {
            String day = LocalDate.now().toString();
            String key = "rec:eval:recall:" + day;

            for (Map.Entry<String, List<Long>> entry : recallMap.entrySet()) {
                String channel = entry.getKey();
                int size = CollectionUtils.isEmpty(entry.getValue()) ? 0 : entry.getValue().size();
                redisTemplate.opsForHash().increment(key, channel + ":users", 1);
                redisTemplate.opsForHash().increment(key, channel + ":items", size);
            }

            redisTemplate.expire(key, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("记录召回评估指标失败, userId={}, err={}", userId, e.getMessage());
        }
    }

    // 评估闭环-融合层：记录用户级融合分数，后续可和点击/点赞做归因评估
    private void recordFusionEval(Long userId, Map<Long, Double> fusionScoreMap) {
        if (userId == null || fusionScoreMap == null || fusionScoreMap.isEmpty()) {
            return;
        }
        try {
            String zsetKey = "rec:eval:fusion:user:" + userId;
            redisTemplate.delete(zsetKey);

            fusionScoreMap.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .limit(MAX_FUSION_EVAL_SIZE)
                    .forEach(e -> redisTemplate.opsForZSet().add(zsetKey, e.getKey().toString(), e.getValue()));

            redisTemplate.expire(zsetKey, 3, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("记录融合评估指标失败, userId={}, err={}", userId, e.getMessage());
        }
    }

    /**
     * User-CF（基于用户协同过滤）
     * 1) 用“浏览+点赞+收藏”构建加权交互向量
     * 2) 计算加权余弦相似度：sim(u,v)=Σ(wu,i*wv,i)/(|wu|*|wv|)
     * 3) 候选文章得分：score(u,i)=Σ(sim(u,v)*wv,i)
     * 4) 排除本人已交互和已曝光，按分数降序返回
     */
    private List<Long> buildCollaborativeFilteringRecommendPool(Long userId, int totalSize) {
        Map<Long, Double> targetWeightMap = loadUserInteractionWeightMap(userId);
        if (targetWeightMap.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> targetArticleIds = targetWeightMap.keySet();

        List<LikeRecord> sameArticleLikes = likeRecordMapper.selectList(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getStatus, "1")
                        .in(LikeRecord::getArticleId, targetArticleIds)
        );

        List<Favorite> sameArticleFavorites = favoritemapper.selectList(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getStatus, "1")
                        .eq(Favorite::getTargetType, "1")
                        .in(Favorite::getTargetId, targetArticleIds)
        );

        List<ArticleViewHistory> sameArticleViews = articleViewHistoryMapper.selectList(
                new LambdaQueryWrapper<ArticleViewHistory>()
                        .in(ArticleViewHistory::getArticleId, targetArticleIds)
        );

        // 候选邻居用户：与目标用户在文章上有交集的用户
        Set<Long> neighborUserIds = new HashSet<>();
        for (LikeRecord record : sameArticleLikes) {
            if (record.getUserId() != null && !Objects.equals(record.getUserId(), userId)) {
                neighborUserIds.add(record.getUserId());
            }
        }
        for (Favorite favorite : sameArticleFavorites) {
            if (favorite.getUserId() != null && !Objects.equals(favorite.getUserId(), userId)) {
                neighborUserIds.add(favorite.getUserId());
            }
        }
        for (ArticleViewHistory view : sameArticleViews) {
            if (view.getUserId() != null && !Objects.equals(view.getUserId(), userId)) {
                neighborUserIds.add(view.getUserId());
            }
        }

        if (CollectionUtils.isEmpty(neighborUserIds)) {
            return Collections.emptyList();
        }

        List<LikeRecord> neighborLikes = likeRecordMapper.selectList(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getStatus, "1")
                        .in(LikeRecord::getUserId, neighborUserIds)
        );

        List<Favorite> neighborFavorites = favoritemapper.selectList(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getStatus, "1")
                        .eq(Favorite::getTargetType, "1")
                        .in(Favorite::getUserId, neighborUserIds)
        );

        List<ArticleViewHistory> neighborViews = articleViewHistoryMapper.selectList(
                new LambdaQueryWrapper<ArticleViewHistory>()
                        .in(ArticleViewHistory::getUserId, neighborUserIds)
                        .orderByDesc(ArticleViewHistory::getCreatedAt)
                        .last("limit 5000")
        );

        // 邻居用户 -> 加权交互向量
        Map<Long, Map<Long, Double>> neighborInteractionMap = new HashMap<>();

        for (LikeRecord record : neighborLikes) {
            if (record.getUserId() == null || record.getArticleId() == null) {
                continue;
            }
            neighborInteractionMap
                    .computeIfAbsent(record.getUserId(), k -> new HashMap<>())
                    .merge(record.getArticleId(), LIKE_BEHAVIOR_WEIGHT, Double::sum);
        }

        for (Favorite favorite : neighborFavorites) {
            if (favorite.getUserId() == null || favorite.getTargetId() == null) {
                continue;
            }
            neighborInteractionMap
                    .computeIfAbsent(favorite.getUserId(), k -> new HashMap<>())
                    .merge(favorite.getTargetId(), FAVORITE_BEHAVIOR_WEIGHT, Double::sum);
        }

        for (ArticleViewHistory view : neighborViews) {
            if (view.getUserId() == null || view.getArticleId() == null) {
                continue;
            }
            neighborInteractionMap
                    .computeIfAbsent(view.getUserId(), k -> new HashMap<>())
                    .merge(view.getArticleId(), VIEW_BEHAVIOR_WEIGHT, Double::sum);
        }

        // 计算目标用户与邻居用户的余弦相似度
        Map<Long, Double> similarityMap = new HashMap<>();
        double targetNorm = vectorNorm(targetWeightMap);
        if (targetNorm <= 0) {
            return Collections.emptyList();
        }

        for (Map.Entry<Long, Map<Long, Double>> entry : neighborInteractionMap.entrySet()) {
            Map<Long, Double> neighborVector = entry.getValue();
            if (neighborVector == null || neighborVector.isEmpty()) {
                continue;
            }

            double dot = dotProduct(targetWeightMap, neighborVector);
            if (dot <= 0) {
                continue;
            }

            double neighborNorm = vectorNorm(neighborVector);
            if (neighborNorm <= 0) {
                continue;
            }

            double similarity = dot / (targetNorm * neighborNorm);

            if (similarity > 0) {
                similarityMap.put(entry.getKey(), similarity);
            }
        }

        if (similarityMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 仅取Top-N近邻，避免长尾噪声
        List<Map.Entry<Long, Double>> topNeighbors = similarityMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(30)
                .collect(Collectors.toList());

        Set<Long> exposedIds = getExposedArticleIds(userId);
        // 候选文章打分：累加近邻相似度
        Map<Long, Double> candidateScoreMap = new HashMap<>();

        for (Map.Entry<Long, Double> neighbor : topNeighbors) {
            Map<Long, Double> neighborVector = neighborInteractionMap.get(neighbor.getKey());
            if (neighborVector == null || neighborVector.isEmpty()) {
                continue;
            }

            double similarity = neighbor.getValue();
            for (Map.Entry<Long, Double> neighborBehavior : neighborVector.entrySet()) {
                Long articleId = neighborBehavior.getKey();
                Double behaviorWeight = neighborBehavior.getValue();
                if (articleId == null || targetArticleIds.contains(articleId) || exposedIds.contains(articleId)) {
                    continue;
                }
                double delta = similarity * (behaviorWeight == null ? VIEW_BEHAVIOR_WEIGHT : behaviorWeight);
                candidateScoreMap.merge(articleId, delta, Double::sum);
            }
        }

        List<Long> result = candidateScoreMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(totalSize)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 协同过滤数量不足时，使用热榜补齐
        if (result.size() < totalSize) {
            Set<Long> excludeIds = new HashSet<>(targetArticleIds);
            excludeIds.addAll(exposedIds);
            excludeIds.addAll(result);

            List<Long> hotFill = baseMapper.queryHotIdsLimit(totalSize * 2);
            for (Long id : hotFill) {
                if (!excludeIds.contains(id)) {
                    result.add(id);
                    if (result.size() >= totalSize) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    // 加载用户历史交互文章（浏览+点赞+收藏）作为隐式反馈集合
    private Set<Long> loadUserInteractionArticleIds(Long userId) {
        Map<Long, Double> weightMap = loadUserInteractionWeightMap(userId);
        return weightMap.keySet();
    }

    // 加载用户行为加权向量：浏览=1, 点赞=3, 收藏=4
    // 向量形式：articleId -> weight
    private Map<Long, Double> loadUserInteractionWeightMap(Long userId) {
        if (userId == null) {
            return Collections.emptyMap();
        }

        Map<Long, Double> weightMap = new HashMap<>();

        List<ArticleViewHistory> viewHistories = articleViewHistoryMapper.selectList(
                new LambdaQueryWrapper<ArticleViewHistory>()
                        .eq(ArticleViewHistory::getUserId, userId)
                        .orderByDesc(ArticleViewHistory::getCreatedAt)
                        .last("limit 200")
        );
        for (ArticleViewHistory history : viewHistories) {
            if (history.getArticleId() != null) {
                weightMap.merge(history.getArticleId(), VIEW_BEHAVIOR_WEIGHT, Double::sum);
            }
        }

        List<LikeRecord> likeRecords = likeRecordMapper.selectList(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, userId)
                        .eq(LikeRecord::getStatus, "1")
        );

        for (LikeRecord likeRecord : likeRecords) {
            if (likeRecord.getArticleId() != null) {
                weightMap.merge(likeRecord.getArticleId(), LIKE_BEHAVIOR_WEIGHT, Double::sum);
            }
        }

        List<Favorite> favorites = favoritemapper.selectList(
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getStatus, "1")
                        .eq(Favorite::getTargetType, "1")
        );

        for (Favorite favorite : favorites) {
            if (favorite.getTargetId() != null) {
                weightMap.merge(favorite.getTargetId(), FAVORITE_BEHAVIOR_WEIGHT, Double::sum);
            }
        }

        return weightMap;
    }

    // 向量点积：Σ(wu,i * wv,i)，仅在交集维度累加
    private double dotProduct(Map<Long, Double> left, Map<Long, Double> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty()) {
            return 0D;
        }
        double result = 0D;
        for (Map.Entry<Long, Double> entry : left.entrySet()) {
            Double rightValue = right.get(entry.getKey());
            if (rightValue != null) {
                result += entry.getValue() * rightValue;
            }
        }
        return result;
    }

    // 向量范数：sqrt(Σ(w^2))，用于余弦相似度归一化
    private double vectorNorm(Map<Long, Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return 0D;
        }
        double sum = 0D;
        for (Double value : vector.values()) {
            if (value != null && value > 0) {
                sum += value * value;
            }
        }
        return Math.sqrt(sum);
    }

    private List<Long> getCachedRecommendIds(String key) {
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return Collections.emptyList();
        }
        List<String> objects =
                redisTemplate.opsForList().range(key, 0, -1);
        if (CollectionUtils.isEmpty(objects)) {
            return Collections.emptyList();
        }
        return objects.stream()
                .map(o -> Long.valueOf(o.toString()))
                .collect(Collectors.toList());
    }

    private List<Long> mixIdList(List<Long> personalize,List<Long> explore) {

        List<Long> result = new ArrayList<>();

        int exploreIndex = 0;
        int personalizeIndex = 0;

        int interval = personalize.size() / (explore.size() + 1);
        if (interval <= 0) interval = 1;

        while (personalizeIndex < personalize.size()
                || exploreIndex < explore.size()) {

            for (int i = 0;
                 i < interval && personalizeIndex < personalize.size();
                 i++) {

                result.add(personalize.get(personalizeIndex++));
            }

            if (exploreIndex < explore.size()) {
                result.add(explore.get(exploreIndex++));
            }
        }

        return result;
    }


    //混排 冷 热 探索打乱重组
    private List<ArticleVO> mixWithExplore(
            List<ArticleVO> personalize,
            List<ArticleVO> explore,
            int pageSize) {

        List<ArticleVO> result = new ArrayList<>();

        int exploreIndex = 0;
        int personalizeIndex = 0;

        int interval = personalize.size() / (explore.size() + 1);
        if (interval <= 0) interval = 1;

        while (result.size() < pageSize &&
                (personalizeIndex < personalize.size()
                        || exploreIndex < explore.size())) {

            // 插入个性化
            for (int i = 0;
                 i < interval
                         && personalizeIndex < personalize.size()
                         && result.size() < pageSize;
                 i++) {

                result.add(personalize.get(personalizeIndex++));
            }

            // 插入探索
            if (exploreIndex < explore.size()
                    && result.size() < pageSize) {

                result.add(explore.get(exploreIndex++));
            }
        }

        return result;
    }



    private Set<Long> extractIds(List<ArticleVO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptySet();
        }
        return list.stream()
                .map(ArticleVO::getId)
                .collect(Collectors.toSet());
    }


    //获取标签权重
    private List<TagWeightDTO> getTopTagWeightsWithDecay(
            Map<Long, Double> tagProfile,
            Map<Long, Long> tagLastTimeMap, // 新增
            int limit) {

        if (tagProfile == null || tagProfile.isEmpty()) {
            return Collections.emptyList();
        }

        long now = System.currentTimeMillis();

        return tagProfile.entrySet().stream()
                .map(entry -> {

                    Long tagId = entry.getKey();
                    Double weight = entry.getValue();


                    Long lastTime = tagLastTimeMap.getOrDefault(tagId, now);
                    if (lastTime == null || lastTime <= 0) {
                        lastTime = now;
                    }
                    double days = (now - lastTime) / (1000.0 * 3600 * 24);

                    // 时间衰减 λ = 0.08
                    double decay = Math.exp(-0.08 * days);

                    double finalWeight = weight * decay;

                    return new TagWeightDTO(tagId, finalWeight, lastTime);
                })
                .sorted((a, b) -> Double.compare(b.getWeight(), a.getWeight()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    //曝光存储规则
    private void recordExpose(Long userId, List<ArticleVO> list) {

        if (userId == null || CollectionUtils.isEmpty(list)) {
            return;
        }

        String key = RedisRecommendKey.userExposeSet(userId);

        for (ArticleVO vo : list) {
            redisTemplate.opsForSet().add(key, vo.getId().toString());
        }

        try {
            String day = LocalDate.now().toString();
            String evalExposeKey = "rec:eval:expose:" + day;
            for (ArticleVO vo : list) {
                redisTemplate.opsForHash().increment(evalExposeKey, vo.getId().toString(), 1);
            }
            redisTemplate.expire(evalExposeKey, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("记录曝光评估指标失败, userId={}, err={}", userId, e.getMessage());
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
        Set<String> set = redisTemplate.opsForSet().members(key);

        if (CollectionUtils.isEmpty(set)) {
            return Collections.emptySet();
        }

        return set.stream()
                .map(o -> Long.valueOf(o.toString()))
                .collect(Collectors.toSet());
    }




    /*  ============================================================================================
        ============================================================================================
             ！！！！！！！！！！！查询首页文章信息(热榜，个性化推荐，关注，最新)结束！！！！！！！！！
        ============================================================================================
        ============================================================================================*/













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

        // 登录用户保存搜索历史
        if (userId != null && keyword != null && !keyword.trim().isEmpty()) {
            saveSearchHistory(userId, keyword.trim());
        }

        ResultSearchVO result = new ResultSearchVO();
        int offset = (pageNum - 1) * pageSize;

    /* =========================
        文章：标题 + 内容（ES）
       ========================= */
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        /* ========= P0：短语完全命中（最高优先级） ========= */
        query.should(
                QueryBuilders.matchPhraseQuery("title", keyword)
                        .boost(500f)
        );
        query.should(
                QueryBuilders.matchPhraseQuery("content", keyword)
                        .boost(3f)
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
            return true;
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

    /**
     * 保存搜索历史：已存在则更新时间，不存在则新增
     */
    private void saveSearchHistory(Long userId, String searchTerm) {
        try {
            SearchHistory existing = searchHistoryMapper.selectByUserAndTerm(userId, searchTerm);
            if (existing != null) {
                searchHistoryMapper.updateLastSearchedAt(userId, searchTerm);
            } else {
                SearchHistory history = new SearchHistory();
                history.setUserId(userId);
                history.setSearchTerm(searchTerm);
                history.setSearchedAt(new Date());
                history.setLastSearchedAt(new Date());
                searchHistoryMapper.insert(history);
            }
        } catch (Exception e) {
            log.warn("保存搜索历史失败: userId={}, term={}, err={}", userId, searchTerm, e.getMessage());
        }
    }



}
