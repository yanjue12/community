package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.mapper.Categorymapper;
import com.fzg.model.Category;
import com.fzg.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-分类管理
 */
@RestController
@RequestMapping("/admin/category")
@SaCheckLogin
public class AdminCategoryController {

    @Autowired
    private Categorymapper categoryMapper;

    /**
     * 分页查询分类列表
     */
    @GetMapping("/list")
    public Result listCategories(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String keyword) {
        Page<Category> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Category::getName, keyword);
        }
        wrapper.orderByAsc(Category::getSort).orderByDesc(Category::getCreatedAt);
        return Result.success(categoryMapper.selectPage(page, wrapper));
    }

    /**
     * 获取所有分类（不分页，树形结构）
     */
    @GetMapping("/tree")
    public Result getCategoryTree() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        return Result.success(categoryMapper.selectList(wrapper));
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public Result getCategory(@PathVariable Long id) {
        Category category = categoryMapper.selectById(id);
        return category != null ? Result.success(category) : Result.fail(404, "分类不存在");
    }

    /**
     * 创建分类
     */
    @PostMapping
    public Result createCategory(@RequestBody Category category) {
        category.setCreatedAt(new java.util.Date());
        int result = categoryMapper.insert(category);
        return Result.handle(result > 0);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public Result updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        category.setUpdatedAt(new java.util.Date());
        int result = categoryMapper.updateById(category);
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
     * 更新分类排序
     */
    @PutMapping("/{id}/sort")
    public Result updateSort(@PathVariable Long id, @RequestParam Integer sort) {
        Category category = new Category();
        category.setId(id);
        category.setSort(sort);
        int result = categoryMapper.updateById(category);
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
