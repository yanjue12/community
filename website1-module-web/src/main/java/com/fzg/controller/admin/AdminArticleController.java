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
import com.fzg.vo.ArticleRequest;
import com.fzg.vo.ArticleVO;
import com.fzg.vo.ArticlePageVO;
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
     * 获取文章详情
     */
    @PostMapping("/query/{id}")
    public Result getArticle(@PathVariable Long id) {
        Article article = articleMapper.selectById(id);
        return article != null ? Result.success(article) : Result.fail(404, "文章不存在");
    }

    /**
     * 删除文章
     */
    @DeleteMapping("delete/{id}")
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
    @PostMapping("/list")
    public Result searchArticles(@RequestBody ArticleRequest request) {
        int pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 查询数据列表
        List<ArticleVO> list = articleMapper.queryArticleByCondition(request, offset);
        
        // 查询总数
        Long total = articleMapper.countArticleByCondition(request);
        
        // 构建分页结果
        ArticlePageVO pageVO = new ArticlePageVO();
        pageVO.setArticleVOList(list);
        pageVO.setTotal(total.intValue());
        
        return Result.success(pageVO);
    }
}
