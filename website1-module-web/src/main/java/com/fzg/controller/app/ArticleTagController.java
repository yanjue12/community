package com.fzg.controller.app;

import com.fzg.mapper.ArticleTagMapper;
import com.fzg.model.ArticleTag;
import com.fzg.model.Result;
import com.fzg.service.ArticleTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
