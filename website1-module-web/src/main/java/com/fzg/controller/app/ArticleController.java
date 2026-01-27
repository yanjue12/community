package com.fzg.controller.app;

import com.alibaba.fastjson2.JSON;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.model.Article;
import com.fzg.model.Result;
import com.fzg.service.ArticleService;
import com.fzg.service.DraftService;
import com.fzg.service.FavoriteService;
import com.fzg.service.LikeRecordService;
import com.fzg.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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



    @PostMapping("/detail")
    public Result details(@RequestBody ArticleRequest articleRequest){
        if(null == articleRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        ArticleDetailVO article = articleService.queryArticleDetails(articleRequest.getArticleId());
        return Result.success(article);
    }



    /**
     * 查询当前登录人发布的文章
     * @param articleRequest
     * @return
     */
    @PostMapping("/queryArticleById")
    public Result queryArticleById(@RequestBody ArticleRequest articleRequest){
        if(null == articleRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }

        Integer pageNum = articleRequest.getPageNum() == null ? 1 : articleRequest.getPageNum();
        Integer pageSize = articleRequest.getPageSize() == null ? 10 : articleRequest.getPageSize();

        List<ArticleVO> articleVOList = articlemapper.queryArticleByUserId(articleRequest.getUserId(),pageSize,(pageNum-1)*pageSize);

        return Result.success(articleVOList);
    }



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
     * 首页搜索：文章标题模糊，，用户昵称模糊，标签模糊，结果分组后返回，type为条件
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
        ResultSearchVO searchVO = articleService.search(searchRequset);

        return Result.success(searchVO);
    }

    /**
     * 搜索框内随机内容展示
     * @return
     */
    @PostMapping("search/suggestions")
    public Result searchSuggestions(){

        List<String> searchSuggestions = articleService.searchSuggestions();
        if(CollectionUtils.isEmpty(searchSuggestions)){
            return Result.fail(EnumReturn.OPERATION_FAIL);
        }
        log.info("搜索框内随机内容展示成功:{}", JSON.toJSONString(searchSuggestions));

        return Result.success(searchSuggestions);
    }



    @PostMapping("/like")
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

