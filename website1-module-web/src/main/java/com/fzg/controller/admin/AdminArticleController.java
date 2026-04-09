package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.AuditRecordMapper;
import com.fzg.mapper.Categorymapper;
import com.fzg.model.Article;
import com.fzg.model.AuditRecord;
import com.fzg.model.Result;
import com.fzg.service.ArticleExportService;
import com.fzg.vo.ArticleRequest;
import com.fzg.vo.ArticleStatsVO;
import com.fzg.vo.ArticleVO;
import com.fzg.vo.ArticlePageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * 管理员-文章管理
 */
@RequestMapping("/admin/article")
@RestController
@SaCheckLogin
@SaCheckRole(value = {"admin", "auditAdmin", "reportAdmin"}, mode = SaMode.OR)
public class AdminArticleController {

    @Autowired
    private Articlemapper articleMapper;
    @Autowired
    private ArticleExportService articleExportService;



    /**
     * 获取文章统计数据（总数、已发布、待审核、本周新增）
     */
    @GetMapping("/statistics")
    public Result getArticleStats() {
        ArticleStatsVO stats = articleMapper.queryArticleStats();
        return Result.success(stats);
    }

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
    @SaCheckRole("admin")
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
    @SaCheckRole("admin")
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
    @PutMapping("/update/status/{id}")
    @SaCheckRole("admin")
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
    @SaCheckRole("admin")
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
    @SaCheckRole("admin")
    public Result recommendArticle(@PathVariable Long id, @RequestParam String isRecommend) {
        Article article = new Article();
        article.setId(id);
        article.setIsRecommend(isRecommend);
        int result = articleMapper.updateById(article);
        return Result.handle(result > 0);
    }

    /**
     * 导出文章统计与明细
     * @param start 开始日期(yyyy-MM-dd)，默认本周一
     * @param end   结束日期(yyyy-MM-dd)，默认今天
     * @param format 导出格式 excel/pdf
     */
    @GetMapping("/export")
    public void exportArticles(@RequestParam(required = false) String start,
                               @RequestParam(required = false) String end,
                               @RequestParam(defaultValue = "excel") String format,
                               javax.servlet.http.HttpServletResponse response) {
        LocalDate s = start == null || start.isBlank() ? null : LocalDate.parse(start);
        LocalDate e = end == null || end.isBlank() ? null : LocalDate.parse(end);
        if ("pdf".equalsIgnoreCase(format)) {
            articleExportService.exportPdf(s, e, response);
        } else {
            articleExportService.exportExcel(s, e, response);
        }
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

        if(null == request){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }


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
