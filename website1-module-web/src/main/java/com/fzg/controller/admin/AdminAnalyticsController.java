package com.fzg.controller.admin;

import com.fzg.dto.analytics.ChartDataDTO;
import com.fzg.dto.analytics.DashboardDTO;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端数据分析控制器
 */
@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "管理端数据分析", description = "数据统计、图表分析相关接口")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * 获取仪表板数据
     */
    @GetMapping("/dashboard")
    @Operation(summary = "获取仪表板数据", description = "获取管理端首页仪表板的所有统计数据")
    public Result<DashboardDTO> getDashboard() {
        try {
            DashboardDTO dashboard = analyticsService.getDashboardData();
            return Result.success(dashboard);
        } catch (Exception e) {
            log.error("获取仪表板数据失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取仪表板数据失败"));
        }
    }

    /**
     * 获取用户增长趋势
     */
    @GetMapping("/user-growth-trend")
    @Operation(summary = "获取用户增长趋势", description = "获取指定天数内的用户增长趋势数据")
    public Result<List<ChartDataDTO.LineItem>> getUserGrowthTrend(
            @Parameter(description = "统计天数", example = "7")
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<ChartDataDTO.LineItem> trend = analyticsService.getUserGrowthTrend(days);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取用户增长趋势失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取用户增长趋势失败"));
        }
    }

    /**
     * 获取文章发布趋势
     */
    @GetMapping("/article-publish-trend")
    @Operation(summary = "获取文章发布趋势", description = "获取指定天数内的文章发布趋势数据")
    public Result<List<ChartDataDTO.LineItem>> getArticlePublishTrend(
            @Parameter(description = "统计天数", example = "7")
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<ChartDataDTO.LineItem> trend = analyticsService.getArticlePublishTrend(days);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取文章发布趋势失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取文章发布趋势失败"));
        }
    }

    /**
     * 获取评论活跃度趋势
     */
    @GetMapping("/comment-activity-trend")
    @Operation(summary = "获取评论活跃度趋势", description = "获取指定天数内的评论活跃度趋势数据")
    public Result<List<ChartDataDTO.LineItem>> getCommentActivityTrend(
            @Parameter(description = "统计天数", example = "7")
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<ChartDataDTO.LineItem> trend = analyticsService.getCommentActivityTrend(days);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取评论活跃度趋势失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取评论活跃度趋势失败"));
        }
    }

    /**
     * 获取浏览量趋势
     */
    @GetMapping("/view-trend")
    @Operation(summary = "获取浏览量趋势", description = "获取指定天数内的浏览量趋势数据")
    public Result<List<ChartDataDTO.LineItem>> getViewTrend(
            @Parameter(description = "统计天数", example = "7")
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<ChartDataDTO.LineItem> trend = analyticsService.getViewTrend(days);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取浏览量趋势失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取浏览量趋势失败"));
        }
    }

    /**
     * 获取文章分类分布
     */
    @GetMapping("/category-distribution")
    @Operation(summary = "获取文章分类分布", description = "获取文章按分类的分布统计，用于扇形图")
    public Result<List<ChartDataDTO.PieItem>> getCategoryDistribution() {
        try {
            List<ChartDataDTO.PieItem> distribution = analyticsService.getCategoryDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取文章分类分布失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取文章分类分布失败"));
        }
    }

    /**
     * 获取标签使用分布
     */
    @GetMapping("/tag-distribution")
    @Operation(summary = "获取标签使用分布", description = "获取标签使用频率分布统计，用于扇形图")
    public Result<List<ChartDataDTO.PieItem>> getTagDistribution(
            @Parameter(description = "限制数量", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ChartDataDTO.PieItem> distribution = analyticsService.getTagDistribution(limit);
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取标签使用分布失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取标签使用分布失败"));
        }
    }

    /**
     * 获取热门文章排行
     */
    @GetMapping("/hot-articles")
    @Operation(summary = "获取热门文章排行", description = "获取指定时间内的热门文章排行榜")
    public Result<List<ChartDataDTO.RankItem>> getHotArticles(
            @Parameter(description = "限制数量", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "统计天数", example = "7")
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<ChartDataDTO.RankItem> hotArticles = analyticsService.getHotArticles(limit, days);
            return Result.success(hotArticles);
        } catch (Exception e) {
            log.error("获取热门文章排行失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取热门文章排行失败"));
        }
    }

    /**
     * 获取活跃用户排行
     */
    @GetMapping("/active-users")
    @Operation(summary = "获取活跃用户排行", description = "获取指定时间内的活跃用户排行榜")
    public Result<List<ChartDataDTO.RankItem>> getActiveUsers(
            @Parameter(description = "限制数量", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "统计天数", example = "7")
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<ChartDataDTO.RankItem> activeUsers = analyticsService.getActiveUsers(limit, days);
            return Result.success(activeUsers);
        } catch (Exception e) {
            log.error("获取活跃用户排行失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取活跃用户排行失败"));
        }
    }

    /**
     * 获取用户增长对比
     */
    @GetMapping("/user-growth-comparison")
    @Operation(summary = "获取用户增长对比", description = "获取用户增长的同比/环比数据")
    public Result<ChartDataDTO.TrendData> getUserGrowthComparison(
            @Parameter(description = "统计周期：day/week/month", example = "day")
            @RequestParam(defaultValue = "day") String period) {
        try {
            ChartDataDTO.TrendData comparison = analyticsService.getUserGrowthComparison(period);
            return Result.success(comparison);
        } catch (Exception e) {
            log.error("获取用户增长对比失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取用户增长对比失败"));
        }
    }

    /**
     * 获取文章发布对比
     */
    @GetMapping("/article-publish-comparison")
    @Operation(summary = "获取文章发布对比", description = "获取文章发布的同比/环比数据")
    public Result<ChartDataDTO.TrendData> getArticlePublishComparison(
            @Parameter(description = "统计周期：day/week/month", example = "day")
            @RequestParam(defaultValue = "day") String period) {
        try {
            ChartDataDTO.TrendData comparison = analyticsService.getArticlePublishComparison(period);
            return Result.success(comparison);
        } catch (Exception e) {
            log.error("获取文章发布对比失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取文章发布对比失败"));
        }
    }
}