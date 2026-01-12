package com.fzg.controller.app;

import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.service.ArticleService;
import com.fzg.vo.ArticleRequest;
import com.fzg.vo.ArticleVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;


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
        List<ArticleVO> articleVOList = articleService.queryListByArticleType(articleRequest);
        return Result.success(articleVOList);
    }

    /**
     * 首页搜索  文章标题模糊，，用户昵称模糊，结果分组后返回，type为条件
     * @param
     * @return
     */
    @PostMapping("/search")
    public Result search(@RequestBody ArticleRequest searchRequset){
        if(null == searchRequset){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }

        return Result.success(true);
    }

}
