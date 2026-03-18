package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.Categorymapper;
import com.fzg.model.Article;
import com.fzg.model.Category;
import com.fzg.model.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-分类管理
 */
@RestController
@RequestMapping("/admin/category")
@SaCheckLogin
@Slf4j
@Tag(name = "管理端分类管理", description = "分类管理相关接口")
public class AdminCategoryController {

    @Autowired
    private Categorymapper categoryMapper;
    
    @Autowired
    private Articlemapper articleMapper;


    /**
     * 获取分类管理统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取分类管理统计", description = "获取分类管理页面的统计数据")
    public Result getCategoryStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 1. 总分类数 - count(*) from category
            Long totalCategories = categoryMapper.selectCount(null);

            // 2. 启用中分类数 - status = '1'
            LambdaQueryWrapper<Category> enabledWrapper = new LambdaQueryWrapper<>();
            enabledWrapper.eq(Category::getStatus, "1");
            Long enabledCategories = categoryMapper.selectCount(enabledWrapper);

            // 3. 总文章数 - count(*) from article
            Long totalArticles = articleMapper.selectCount(null);

            // 4. 已归档分类数 - status = '0' (禁用的分类视为已归档)
            LambdaQueryWrapper<Category> archivedWrapper = new LambdaQueryWrapper<>();
            archivedWrapper.eq(Category::getStatus, "0");
            Long archivedCategories = categoryMapper.selectCount(archivedWrapper);

            // 5. 各级分类统计
            // 一级分类数量
            LambdaQueryWrapper<Category> level1Wrapper = new LambdaQueryWrapper<>();
            level1Wrapper.eq(Category::getLevel, "1");
            Long level1Count = categoryMapper.selectCount(level1Wrapper);

            // 二级分类数量
            LambdaQueryWrapper<Category> level2Wrapper = new LambdaQueryWrapper<>();
            level2Wrapper.eq(Category::getLevel, "2");
            Long level2Count = categoryMapper.selectCount(level2Wrapper);

            // 三级分类数量
            LambdaQueryWrapper<Category> level3Wrapper = new LambdaQueryWrapper<>();
            level3Wrapper.eq(Category::getLevel, "3");
            Long level3Count = categoryMapper.selectCount(level3Wrapper);

            // 6. 推荐分类数量
            LambdaQueryWrapper<Category> recommendWrapper = new LambdaQueryWrapper<>();
            recommendWrapper.eq(Category::getIsRecommend, "1");
            Long recommendCategories = categoryMapper.selectCount(recommendWrapper);

            // 7. 导航显示分类数量
            LambdaQueryWrapper<Category> navWrapper = new LambdaQueryWrapper<>();
            navWrapper.eq(Category::getIsNav, "1");
            Long navCategories = categoryMapper.selectCount(navWrapper);

            // 组装统计数据
            statistics.put("totalCategories", totalCategories);
            statistics.put("enabledCategories", enabledCategories);
            statistics.put("totalArticles", totalArticles);
            statistics.put("archivedCategories", archivedCategories);
            statistics.put("level1Count", level1Count);
            statistics.put("level2Count", level2Count);
            statistics.put("level3Count", level3Count);
            statistics.put("recommendCategories", recommendCategories);
            statistics.put("navCategories", navCategories);

            // 计算启用率
            double enabledRate = totalCategories > 0 ? (double) enabledCategories / totalCategories * 100 : 0;
            statistics.put("enabledRate", String.format("%.1f%%", enabledRate));

            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取分类统计数据失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取统计数据失败"));
        }
    }


    /**
     * 创建分类
     */
    @PostMapping("/create")
    public Result createCategory(@RequestBody Category category) {
        category.setCreatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
        int result = categoryMapper.insert(category);
        return Result.handle(result > 0);
    }


    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result deleteCategory(@PathVariable Long id) {
        // 检查是否有子分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, id);
        Long count = categoryMapper.selectCount(wrapper);
        if (count > 0) {
            return Result.fail(400, "该分类下有子分类，无法删除");
        }
        int result = categoryMapper.deleteById(id);
        return Result.handle(result > 0);
    }

    /**
     * 批量删除分类
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody java.util.List<Long> ids) {
        int result = categoryMapper.deleteBatchIds(ids);
        return Result.handle(result > 0);
    }



    /**
     * 启用/禁用分类
     */
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestParam String status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        int result = categoryMapper.updateById(category);
        return Result.handle(result > 0);
    }


}
