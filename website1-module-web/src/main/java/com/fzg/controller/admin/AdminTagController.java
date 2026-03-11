package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.mapper.ArticleTagMapper;
import com.fzg.model.ArticleTag;
import com.fzg.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-标签管理
 */
@RestController
@RequestMapping("/admin/tag")
@SaCheckLogin
public class AdminTagController {

    @Autowired
    private ArticleTagMapper tagMapper;

    /**
     * 分页查询标签列表
     */
    @GetMapping("/list")
    public Result listTags(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String keyword) {
        Page<ArticleTag> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ArticleTag::getTagName, keyword);
        }
        wrapper.orderByDesc(ArticleTag::getCreateTime);
        return Result.success(tagMapper.selectPage(page, wrapper));
    }

    /**
     * 获取所有标签（不分页）
     */
    @GetMapping("/all")
    public Result getAllTags() {
        return Result.success(tagMapper.selectList(null));
    }

    /**
     * 获取标签详情
     */
    @GetMapping("/{id}")
    public Result getTag(@PathVariable Long id) {
        ArticleTag tag = tagMapper.selectById(id);
        return tag != null ? Result.success(tag) : Result.fail(404, "标签不存在");
    }

    /**
     * 创建标签
     */
    @PostMapping
    public Result createTag(@RequestBody ArticleTag tag) {
        tag.setCreateTime(new java.util.Date());
        int result = tagMapper.insert(tag);
        return Result.handle(result > 0);
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public Result updateTag(@PathVariable Long id, @RequestBody ArticleTag tag) {
        tag.setId(id);
        tag.setUpdateTime(new java.util.Date());
        int result = tagMapper.updateById(tag);
        return Result.handle(result > 0);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public Result deleteTag(@PathVariable Long id) {
        int result = tagMapper.deleteById(id);
        return Result.handle(result > 0);
    }

    /**
     * 批量删除标签
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody java.util.List<Long> ids) {
        int result = tagMapper.deleteBatchIds(ids);
        return Result.handle(result > 0);
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/hot")
    public Result getHotTags(@RequestParam(defaultValue = "10") Integer limit) {
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ArticleTag::getUseCount).last("LIMIT " + limit);
        return Result.success(tagMapper.selectList(wrapper));
    }
}
