package com.fzg.controller.app;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.mapper.Categorymapper;
import com.fzg.model.Category;
import com.fzg.model.Result;
import com.fzg.vo.CategoryTreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类管理控制器
 */
@RequestMapping("/category")
@RestController
public class CategoryController {

    @Autowired
    private Categorymapper categorymapper;

    /**
     * 获取分类树形结构列表
     */
    @PostMapping("/list")
    public Result queryCategory(){
        // 获取所有一级分类（parentId = 0）
        LambdaQueryWrapper<Category> q = new LambdaQueryWrapper<>();
        q.eq(Category::getStatus,"1")
                .eq(Category::getParentId,0L)
                .orderByAsc(Category::getId);
        List<Category> categories = categorymapper.selectList(q);
        
        // 为每个一级分类构建子分类树
        List<CategoryTreeVO> categoryTree = categories.stream().map(category -> {
            CategoryTreeVO treeNode = new CategoryTreeVO();
            treeNode.setId(category.getId());
            treeNode.setCategoryName(category.getName());
            treeNode.setParentId(category.getParentId());
            treeNode.setStatus(category.getStatus());
            
            // 获取所有子分类（不限制层级）
            List<CategoryTreeVO> children = getChildCategories(category.getId());
            treeNode.setChildren(children);
            
            return treeNode;
        }).collect(Collectors.toList());
        
        return Result.success(categoryTree);
    }

    /**
     * 递归获取子分类（不限制层级）
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    private List<CategoryTreeVO> getChildCategories(Long parentId) {
        LambdaQueryWrapper<Category> q = new LambdaQueryWrapper<>();
        q.eq(Category::getStatus,"1")
                .eq(Category::getParentId, parentId)
                .orderByAsc(Category::getId);
        List<Category> children = categorymapper.selectList(q);
        
        return children.stream().map(category -> {
            CategoryTreeVO treeNode = new CategoryTreeVO();
            treeNode.setId(category.getId());
            treeNode.setCategoryName(category.getName());
            treeNode.setParentId(category.getParentId());
            treeNode.setStatus(category.getStatus());
            
            // 递归获取所有子分类
            List<CategoryTreeVO> grandChildren = getChildCategories(category.getId());
            treeNode.setChildren(grandChildren);
            
            return treeNode;
        }).collect(Collectors.toList());
    }

}