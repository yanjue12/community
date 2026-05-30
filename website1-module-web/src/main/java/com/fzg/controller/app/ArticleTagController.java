package com.fzg.controller.app;

import com.fzg.mapper.ArticleTagMapper;
import com.fzg.model.ArticleTag;
import com.fzg.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/articleTag")
public class ArticleTagController {

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @PostMapping("/queryHotTags")
    public Result queryList(){
        // 查询最热门的10个标签
        List<ArticleTag> articleTags = articleTagMapper.queryHotTags();
        return Result.success(articleTags);
    }

    /**
     * 引导式注册：获取 Top N 热门标签供新用户选择
     * 用于解决推荐系统冷启动问题
     */
    @PostMapping("/queryGuideTags")
    public Result queryGuideTags(@RequestParam(value = "limit", required = false, defaultValue = "30") Integer limit) {
        if (limit == null || limit <= 0 || limit > 100) {
            limit = 30;
        }
        List<ArticleTag> tags = articleTagMapper.queryGuideTags(limit);
        return Result.success(tags);
    }
}
