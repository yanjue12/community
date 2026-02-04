package com.fzg.controller.app;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.Followmapper;
import com.fzg.model.Follow;
import com.fzg.model.Result;
import com.fzg.model.UserPrivacy;
import com.fzg.service.*;
import com.fzg.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Result details(@RequestBody ArticleRequest articleRequest){
        if(null == articleRequest || StringUtils.isEmpty(articleRequest.getIp())
        || null == articleRequest.getArticleId()){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        ArticleDetailVO article = articleService.queryArticleDetails(articleRequest);
        return Result.success(article);
    }



        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        // @@@@@@@@@@@           个人主页数据请求            @@@@@@@@@@@@@@@@@@@@
        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@


     /* 查询发布的文章 个人主页作品展示
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
        List<ArticleVO> articleVOList = new ArrayList<>();
        if(articleRequest.getUserId() == articleRequest.getAuthorId()){
            //说明查看的是自己的主页作品
            articleVOList = articlemapper.queryArticleByUserId(articleRequest.getUserId(),pageSize,(pageNum-1)*pageSize);
        } else {
            //查看他人的主页作品 需要判断作者的隐私设置
            LambdaQueryWrapper<UserPrivacy> u = new LambdaQueryWrapper<>();
            u.eq(UserPrivacy::getUserId,articleRequest.getAuthorId());
            UserPrivacy userPrivacy = userPrivacyService.getOne(u);
            String artvis = userPrivacy.getArticleVisibility();
            //判断隐私权限
            if("0".equals(artvis)){
                articleVOList = articlemapper.queryArticleByUserId(articleRequest.getAuthorId(),pageSize,(pageNum-1)*pageSize);
            }else if("1".equals(artvis)){
                //私密
                return Result.success(articleVOList);
            }else if("2".equals(artvis)){
                //粉丝可见
                LambdaQueryWrapper<Follow> f = new LambdaQueryWrapper<>();
                f.eq(Follow::getFollowerId,articleRequest.getUserId())
                 .eq(Follow::getFollowingId,articleRequest.getAuthorId());
                Follow follow = followmapper.selectOne(f);
                if(null == follow){
                    return Result.success(articleVOList);
                }
                articleVOList = articlemapper.queryArticleByUserId(articleRequest.getAuthorId(),pageSize,(pageNum-1)*pageSize);
            }else if("3".equals(artvis)){
                //互相关注
                LambdaQueryWrapper<Follow> f = new LambdaQueryWrapper<>();
                f.eq(Follow::getFollowerId,articleRequest.getUserId())
                        .eq(Follow::getFollowingId,articleRequest.getAuthorId());
                Follow follow = followmapper.selectOne(f);
                if(null == follow){
                    return Result.success(articleVOList);
                }
                f.clear();
                f.eq(Follow::getFollowerId,articleRequest.getAuthorId())
                        .eq(Follow::getFollowingId,articleRequest.getUserId());
                Follow follow2 = followmapper.selectOne(f);
                if(null == follow2){
                    return Result.success(articleVOList);
                }
                articleVOList = articlemapper.queryArticleByUserId(articleRequest.getAuthorId(),pageSize,(pageNum-1)*pageSize);
            }
        }

        return Result.success(articleVOList);
    }

    /**
     * 查询用户喜欢列表
     * @param articleRequest
     * @return
     */
    @PostMapping("queryLikeArtById")
    public Result queryArtLikeById(@RequestBody ArticleRequest articleRequest){
        if(null == articleRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }

        Integer pageNum = articleRequest.getPageNum() == null ? 1 : articleRequest.getPageNum();
        Integer pageSize = articleRequest.getPageSize() == null ? 10 : articleRequest.getPageSize();

        List<ArticleVO> articleVOList = new ArrayList<>();
        if(articleRequest.getUserId() == articleRequest.getAuthorId()){
            //说明查看的是自己的主页作品
            articleVOList = articlemapper.queryArtLikeById(articleRequest.getUserId(),pageSize,(pageNum-1)*pageSize);
        } else {
            //查看他人的主页作品 需要判断作者的隐私设置
            LambdaQueryWrapper<UserPrivacy> u = new LambdaQueryWrapper<>();
            u.eq(UserPrivacy::getUserId, articleRequest.getAuthorId());
            UserPrivacy userPrivacy = userPrivacyService.getOne(u);
            String artvis = userPrivacy.getArticleVisibility();
            if("0".equals(artvis)){
                articleVOList = articlemapper.queryArtLikeById(articleRequest.getAuthorId(),pageSize,(pageNum-1)*pageSize);
            }else if("1".equals(artvis)){
                //私密
                return Result.success(articleVOList);
            }else if("2".equals(artvis)){
                //粉丝可见
                LambdaQueryWrapper<Follow> f = new LambdaQueryWrapper<>();
                f.eq(Follow::getFollowerId,articleRequest.getUserId())
                        .eq(Follow::getFollowingId,articleRequest.getAuthorId());
                Follow follow = followmapper.selectOne(f);
                if(null == follow){
                    return Result.success(articleVOList);
                }
                articleVOList = articlemapper.queryArtLikeById(articleRequest.getAuthorId(),pageSize,(pageNum-1)*pageSize);
            }else if("3".equals(artvis)){
                //互相关注
                LambdaQueryWrapper<Follow> f = new LambdaQueryWrapper<>();
                f.eq(Follow::getFollowerId,articleRequest.getUserId())
                        .eq(Follow::getFollowingId,articleRequest.getAuthorId());
                Follow follow = followmapper.selectOne(f);
                if(null == follow){
                    return Result.success(articleVOList);
                }
                f.clear();
                f.eq(Follow::getFollowerId,articleRequest.getAuthorId())
                        .eq(Follow::getFollowingId,articleRequest.getUserId());
                Follow follow2 = followmapper.selectOne(f);
                if(null == follow2){
                    return Result.success(articleVOList);
                }
                articleVOList = articlemapper.queryArtLikeById(articleRequest.getAuthorId(),pageSize,(pageNum-1)*pageSize);
            }
        }

        return Result.success(articleVOList);
    }

    /**
     * 查询收藏列表
     * @param articleRequest
     * @return
     */
    @PostMapping("queryFavArtById")
    public Result queryFavoriteArtById(@RequestBody ArticleRequest articleRequest){
        if(null == articleRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        Integer pageNum = articleRequest.getPageNum() == null ? 1 : articleRequest.getPageNum();
        Integer pageSize = articleRequest.getPageSize() == null ? 10 : articleRequest.getPageSize();

        List<ArticleVO> articleVOList = new ArrayList<>();
        if(articleRequest.getUserId() == articleRequest.getAuthorId()){
            //说明查看的是自己的主页作品
            articleVOList = articleService.queryFavoriteArtById(articleRequest.getUserId(),pageSize,(pageNum-1)*pageSize);
        } else {
            //查看他人的主页作品 需要判断作者的隐私设置
            LambdaQueryWrapper<UserPrivacy> u = new LambdaQueryWrapper<>();
            u.eq(UserPrivacy::getUserId, articleRequest.getAuthorId());
            UserPrivacy userPrivacy = userPrivacyService.getOne(u);
            String artvis = userPrivacy.getArticleVisibility();
            if("0".equals(artvis)){
                articleVOList = articleService.queryFavoriteArtById(articleRequest.getAuthorId(),pageSize,(pageNum-1)*pageSize);
            }else if("1".equals(artvis)){
                //私密
                return Result.success(articleVOList);
            }else if("2".equals(artvis)){
                //粉丝可见
                LambdaQueryWrapper<Follow> f = new LambdaQueryWrapper<>();
                f.eq(Follow::getFollowerId,articleRequest.getUserId())
                        .eq(Follow::getFollowingId,articleRequest.getAuthorId());
                Follow follow = followmapper.selectOne(f);
                if(null == follow){
                    return Result.success(articleVOList);
                }
                articleVOList = articleService.queryFavoriteArtById(articleRequest.getAuthorId(),pageSize,(pageNum-1)*pageSize);
            }else if("3".equals(artvis)){
                //互相关注
                LambdaQueryWrapper<Follow> f = new LambdaQueryWrapper<>();
                f.eq(Follow::getFollowerId,articleRequest.getUserId())
                        .eq(Follow::getFollowingId,articleRequest.getAuthorId());
                Follow follow = followmapper.selectOne(f);
                if(null == follow){
                    return Result.success(articleVOList);
                }
                f.clear();
                f.eq(Follow::getFollowerId,articleRequest.getAuthorId())
                        .eq(Follow::getFollowingId,articleRequest.getUserId());
                Follow follow2 = followmapper.selectOne(f);
                if(null == follow2){
                    return Result.success(articleVOList);
                }
                articleVOList = articleService.queryFavoriteArtById(articleRequest.getAuthorId(),pageSize,(pageNum-1)*pageSize);
            }
        }

        return Result.success(articleVOList);
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

    @PostMapping("deleteArt")
    public Result deleteArt(@RequestBody ArticleRequest articleRequest){
        //StpUtil.checkLogin();
        if(null == articleRequest){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }

        int b = articlemapper.deleteById(articleRequest.getArticleId());

        return b > 0 ? Result.success(true) : Result.fail(EnumReturn.OPERATION_FAIL);
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

