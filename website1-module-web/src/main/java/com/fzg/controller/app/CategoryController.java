package com.fzg.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.mapper.Categorymapper;
import com.fzg.model.Category;
import com.fzg.model.Result;
import com.fzg.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private Categorymapper categorymapper;

    @PostMapping("/list")
    public Result queryCategory(){
        LambdaQueryWrapper<Category> q = new LambdaQueryWrapper<>();
        q.eq(Category::getStatus,"1")
                .eq(Category::getParentId,0L);
        List<Category> categories = categorymapper.selectList(q);
        return Result.success(categories);
    }
}
