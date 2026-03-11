package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.AuditRecordMapper;
import com.fzg.model.Article;
import com.fzg.model.AuditRecord;
import com.fzg.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 管理员-文章管理
 */
@RequestMapping("/admin/article")
@RestController
@SaCheckLogin
public class AdminArticleController {

    @Autowired
    private Articlemapper articleMapper;
    @Autowired
    private AuditRecordMapper auditRecordMapper;

    /**
     * 分页查询文章列表
     */
    @GetMapping("/list")
    public Result listArticles(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer status) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Article::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        wrapper.orderByDesc(Article::getCreatedAt);
        return Result.success(articleMapper.selectPage(page, wrapper));
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/query/{id}")
    public Result getArticle(@PathVariable Long id) {
        Article article = articleMapper.selectById(id);
        return article != null ? Result.success(article) : Result.fail(404, "文章不存在");
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result deleteArticle(@PathVariable Long id) {
        Article article = articleMapper.selectById(id);
        if(null == article){
            return Result.fail(EnumReturn.valueOf("文章不存在"));
        }
        article.setStatus("4");
        int result = articleMapper.updateById(article);
        return Result.handle(result > 0);
    }

    /**
     * 批量删除文章
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody List<Long> ids) {
        List<Article> articles = articleMapper.selectBatchIds(ids);
        if(CollectionUtils.isEmpty(articles)){
            return Result.fail(EnumReturn.valueOf("文章不存在"));
        }
        for (Article article : articles) {
            article.setStatus("4");
        }
        int result = articleMapper.updateBatchById(ids);
        return Result.handle(result > 0);
    }

    /**
     * 更新文章状态
     */
    @PutMapping("/update/status")
    public Result updateStatus(@PathVariable Long id, @RequestParam String status) {
        Article article = articleMapper.selectById(id);
        if(null == article){
            return Result.fail(EnumReturn.valueOf("文章不存在"));
        }
        article.setStatus(status);
        int result = articleMapper.updateById(article);
        return Result.handle(result > 0);
    }

    /**
     * 置顶文章
     */
    @PutMapping("/{id}/top")
    public Result topArticle(@PathVariable Long id, @RequestParam String isTop) {
        Article article = new Article();
        article.setId(id);
        article.setIsTop(isTop);
        int result = articleMapper.updateById(article);
        return Result.handle(result > 0);
    }

    /**
     * 推荐文章
     */
    @PutMapping("/{id}/recommend")
    public Result recommendArticle(@PathVariable Long id, @RequestParam String isRecommend) {
        Article article = new Article();
        article.setId(id);
        article.setIsRecommend(isRecommend);
        int result = articleMapper.updateById(article);
        return Result.handle(result > 0);
    }

    /**
     * 手动审核通过
     */
    @PostMapping("/audit/manual/pass")
    @Transactional(rollbackFor = Exception.class)
    public Result manualPass(@RequestParam Long articleId, @RequestParam Long adminId) {
        Article article = new Article();
        article.setId(articleId);
        article.setStatus("1"); // 1-已发布
        int result = articleMapper.updateById(article);
        
        if (result > 0) {
            // 记录审核记录
            AuditRecord record = new AuditRecord();
            record.setArticleId(articleId);
            record.setAuditType((byte) 2);
            record.setAuditorId(adminId);
            record.setAuditStatus((byte) 1);
            record.setUpdatedAt(new java.util.Date());
            auditRecordMapper.insert(record);
        }
        return Result.handle(result > 0);
    }

    /**
     * 手动审核拒绝
     */
    @PostMapping("/audit/manual/reject")
    @Transactional(rollbackFor = Exception.class)
    public Result manualReject(@RequestParam Long articleId, 
                               @RequestParam Long adminId,
                               @RequestParam String reason) {
        Article article = new Article();
        article.setId(articleId);
        article.setStatus("2"); // 2-审核拒绝
        int result = articleMapper.updateById(article);
        
        if (result > 0) {
            AuditRecord record = new AuditRecord();
            record.setArticleId(articleId);
            record.setAuditorId(adminId);
            record.setAuditStatus((byte) 2);
            record.setReason(reason);
            record.setUpdatedAt(new Date());
            auditRecordMapper.insert(record);
        }
        return Result.handle(result > 0);
    }

    /**
     * 多维度搜索文章
     * 支持：标题、作者ID、作者名称、时间范围、审核状态、置顶、推荐等多条件组合搜索
     */
    @GetMapping("/search")
    public Result searchArticles(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String title,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String isTop,
                                 @RequestParam(required = false) String isRecommend,
                                 @RequestParam(required = false) Long categoryId,
                                 @RequestParam(required = false) String startTime,
                                 @RequestParam(required = false) String endTime,
                                 @RequestParam(required = false) Integer minViews,
                                 @RequestParam(required = false) Integer maxViews,
                                 @RequestParam(required = false) Integer minLikes,
                                 @RequestParam(required = false) Integer maxLikes) {
        
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        
        // 标题模糊搜索
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(Article::getTitle, title.trim());
        }

        
        // 审核状态精确搜索
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Article::getStatus, status.trim());
        }
        
        // 是否置顶
        if (isTop != null && !isTop.trim().isEmpty()) {
            wrapper.eq(Article::getIsTop, isTop.trim());
        }
        
        // 是否推荐
        if (isRecommend != null && !isRecommend.trim().isEmpty()) {
            wrapper.eq(Article::getIsRecommend, isRecommend.trim());
        }
        
        // 分类ID
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        
        // 时间范围搜索
        if (startTime != null && !startTime.trim().isEmpty()) {
            wrapper.ge(Article::getCreatedAt, startTime.trim());
        }
        if (endTime != null && !endTime.trim().isEmpty()) {
            wrapper.le(Article::getCreatedAt, endTime.trim());
        }
        
        // 浏览量范围
        if (minViews != null) {
            wrapper.ge(Article::getViewCount, minViews);
        }
        if (maxViews != null) {
            wrapper.le(Article::getViewCount, maxViews);
        }
        
        // 点赞量范围
        if (minLikes != null) {
            wrapper.ge(Article::getLikeCount, minLikes);
        }
        if (maxLikes != null) {
            wrapper.le(Article::getLikeCount, maxLikes);
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc(Article::getCreatedAt);
        
        Page<Article> result = articleMapper.selectPage(page, wrapper);
        return Result.success(result);
    }
}
