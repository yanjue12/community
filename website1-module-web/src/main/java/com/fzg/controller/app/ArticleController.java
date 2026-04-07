package com.fzg.controller.app;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.annotation.ArticleViewTrack;
import com.fzg.enums.ArticleListType;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.Followmapper;
import com.fzg.model.ArticleEs;
import com.fzg.model.Follow;
import com.fzg.model.Result;
import com.fzg.model.UserPrivacy;
import com.fzg.service.*;
import com.fzg.vo.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController()
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private LikeRecordService likeRecordService;
    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private Articlemapper articlemapper;
    @Autowired
    private ArticleStatService articleStatService;
    @Autowired
    private UserPrivacyService userPrivacyService;
    @Autowired
    private Followmapper followmapper;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;


    /**
     * 统计阅读时间，获取当天热点值，参与主页热点数据排序
     * @param req
     */
    @PostMapping("/readTime")
    public void recordReadTime(@RequestBody ReadTimeRequest req) {
        log.info("recordReadTime开始执行");
        articleStatService.recordReadTime(
                req.getArticleId(),
                req.getUserId(),
                req.getDuration(),
                req.getIp()
        );
    }


    @PostMapping("/detail")
    @ArticleViewTrack
    public Result details(@RequestBody ArticleRequest articleRequest){
        if(null == articleRequest || StringUtils.isEmpty(articleRequest.getIp())
        || null == articleRequest.getArticleId()){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        ArticleDetailVO article = articleService.queryArticleDetails(articleRequest);
        return Result.success(article);
    }




    @PostMapping("/queryArticleById")
    @Schema(description = "查询发布的文章")
    public Result queryArticleById(@RequestBody ArticleRequest req) {
        return Result.success(
                articleService.queryWithVisibility(req, ArticleListType.PUBLISH)
        );
    }

    @PostMapping("/queryLikeArtById")
    @Schema(description = "查询用户喜欢列表")
    public Result queryArtLikeById(@RequestBody ArticleRequest req) {
        return Result.success(
                articleService.queryWithVisibility(req, ArticleListType.LIKE)
        );
    }

    @PostMapping("/queryFavArtById")
    @Schema(description = "查询用户收藏列表")
    public Result queryFavoriteArtById(@RequestBody ArticleRequest req) {
        return Result.success(
                articleService.queryWithVisibility(req, ArticleListType.FAVORITE)
        );
    }






    /**
     * 查询待审核内容
     * @param articleRequest
     * @return
     */
    @PostMapping("/queryPendingArticles")
    public Result queryPendingArticles(@RequestBody ArticleRequest articleRequest){
        if(null == articleRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }

        Integer pageNum = articleRequest.getPageNum() == null ? 1 : articleRequest.getPageNum();
        Integer pageSize = articleRequest.getPageSize() == null ? 10 : articleRequest.getPageSize();

        List<ArticleVO> articleVOList = articlemapper.queryArtPendingById(articleRequest.getUserId(),pageSize,(pageNum-1)*pageSize);
        return Result.success(articleVOList);

    }

    /**
     * 撤回待审核内容 撤回后保存到草稿箱
     * @param articleRequest
     * @return
     */
    @PostMapping("/recallPendingArticles")
    public Result recallPendingArticles(@RequestBody ArticleRequest articleRequest){
        if(null == articleRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }

        Boolean b = articleService.recallPendingArticles(articleRequest);

        return b ? Result.success(true) : Result.fail(EnumReturn.OPERATION_FAIL);
    }

    @PostMapping("/deleteArt")
    @Transactional
    public Result deleteArt(@RequestBody ArticleRequest articleRequest){
        //StpUtil.checkLogin();
        if(null == articleRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        Long articleId = articleRequest.getArticleId();
        if(null == articleId){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        int b = articlemapper.deleteById(articleId);
        // 2. ES 删除
        elasticsearchRestTemplate.delete(articleId.toString(), ArticleEs.class);

        return Result.handle(b > 0);
    }






    /** @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
     * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
     * @@@@@@@@@@@           个人主页数据请求结束            @@@@@@@@@@@@@@@@@@@@
     * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
     * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
     * @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
     */



    /**
     * 首页数据查询
     * @param articleRequest
     * @return
     */
    @PostMapping("/queryList")
    public Result queryList(@RequestBody ArticleRequest articleRequest){
        if(null == articleRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        if(StringUtils.isEmpty(articleRequest.getType())){
            return Result.fail(EnumReturn.MENU_TYPE_IS_EMPTY);
        }

        ArticlePageVO articleVOList = articleService.queryListByArticleType(articleRequest);
        return Result.success(articleVOList);
    }

    /**
     * 首页搜索：文章标题内容，，用户昵称模糊，标签，结果分组后返回，type为条件
     * @param
     * @return
     */
    @PostMapping("/search")
    public Result search(@RequestBody ArticleRequest searchRequset){
        if(null == searchRequset){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        if(StringUtils.isEmpty(searchRequset.getType())){
            return Result.fail(EnumReturn.QUERY_PARAM_EMPTY);
        }
        ResultSearchVO searchVO = articleService.searchArticleByEs(searchRequset);

        return Result.success(searchVO);
    }

    /**
     * 搜索框内随机内容展示
     * @return
     */
    @PostMapping("/search/suggestions")
    @Schema(description = "搜索框内随机内容展示")
    public Result searchSuggestions(){

        List<String> searchSuggestions = articleService.searchSuggestions();
        if(CollectionUtils.isEmpty(searchSuggestions)){
            return Result.fail(EnumReturn.OPERATION_FAIL);
        }
        log.info("搜索框内随机内容展示成功:{}", JSON.toJSONString(searchSuggestions));

        return Result.success(searchSuggestions);
    }



    @PostMapping("/like")
    @Schema(description = "获取点赞列表")
    public Result like(@RequestBody LikeRequest likeRequest){
        if(null == likeRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        if(StringUtils.isEmpty(likeRequest.getType())){
            return Result.fail(EnumReturn.QUERY_PARAM_EMPTY);
        }
        if(null == likeRequest.getArticleId() || null == likeRequest.getUserId() ||
        null == likeRequest.getActionLike()){
            return Result.fail(EnumReturn.QUERY_PARAM_EMPTY);
        }

        return likeRecordService.articleLike(likeRequest);
    }


    @PostMapping("/favorite")
    public Result collect(@RequestBody LikeRequest likeRequest){
        if(null == likeRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        if(StringUtils.isEmpty(likeRequest.getType())){
            return Result.fail(EnumReturn.QUERY_PARAM_EMPTY);
        }
        if(null == likeRequest.getArticleId() || null == likeRequest.getUserId() ||
                null == likeRequest.getActionLike()){
            return Result.fail(EnumReturn.QUERY_PARAM_EMPTY);
        }

        return favoriteService.articleCollect(likeRequest);

    }




}

