package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.fzg.dto.analytics.ChartDataDTO;
import com.fzg.dto.analytics.DashboardDTO;
import com.fzg.dto.analytics.RealtimeActivityDTO;
import com.fzg.enums.EnumReturn;
import com.fzg.model.AuditRecord;
import com.fzg.model.Report;
import com.fzg.model.Result;
import com.fzg.service.AnalyticsExportService;
import com.fzg.service.AnalyticsService;
import com.fzg.service.AuditRecordService;
import com.fzg.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端数据分析控制器
 */
@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
@Slf4j
@SaCheckLogin
@SaCheckRole(value = {"admin", "auditAdmin", "reportAdmin"}, mode = SaMode.OR)
@Tag(name = "管理端数据分析", description = "数据统计、图表分析相关接口")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsExportService analyticsExportService;
    private final ReportService reportService;
    private final AuditRecordService auditRecordService;

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
     * 获取用户增长趋势（支持多种时间维度）
     */
    @GetMapping("/user-growth-trend")
    @Operation(summary = "获取用户增长趋势", description = "支持本日、本月、本年或自定义时间段的用户增长趋势")
    public Result<List<ChartDataDTO.LineItem>> getUserGrowthTrend(
            @Parameter(description = "时间类型：today/thisMonth/thisYear/custom", example = "today")
            @RequestParam String timeType,
            @Parameter(description = "开始日期（自定义时间段时使用，格式：yyyy-MM-dd）", example = "2026-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（自定义时间段时使用，格式：yyyy-MM-dd）", example = "2026-01-31")
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = null;
            LocalDate end = null;
            
            if ("custom".equalsIgnoreCase(timeType)) {
                if (startDate == null || endDate == null) {
                    return Result.fail(EnumReturn.valueOf("自定义时间段需要提供开始和结束日期"));
                }
                start = LocalDate.parse(startDate);
                end = LocalDate.parse(endDate);
            }
            
            List<ChartDataDTO.LineItem> trend = analyticsService.getUserGrowthTrend(timeType, start, end);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取用户增长趋势失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取用户增长趋势失败"));
        }
    }



    /**
     * 获取文章发布趋势（支持多种时间维度）
     */
    @GetMapping("/article-publish-trend-advanced")
    @Operation(summary = "获取文章发布趋势（高级）", description = "支持本日、本月、本年或自定义时间段的文章发布趋势，包含草稿数据")
    public Result<List<ChartDataDTO.LineItem>> getArticlePublishTrendAdvanced(
            @Parameter(description = "时间类型：today/thisMonth/thisYear/custom", example = "today")
            @RequestParam String timeType,
            @Parameter(description = "开始日期（自定义时间段时使用，格式：yyyy-MM-dd）", example = "2026-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（自定义时间段时使用，格式：yyyy-MM-dd）", example = "2026-01-31")
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = null;
            LocalDate end = null;
            
            if ("custom".equalsIgnoreCase(timeType)) {
                if (startDate == null || endDate == null) {
                    return Result.fail(EnumReturn.valueOf("自定义时间段需要提供开始和结束日期"));
                }
                start = LocalDate.parse(startDate);
                end = LocalDate.parse(endDate);
            }
            
            List<ChartDataDTO.LineItem> trend = analyticsService.getArticlePublishTrend(timeType, start, end);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取文章发布趋势失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取文章发布趋势失败"));
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
     * 获取实时动态
     */
    @GetMapping("/realtime-activities")
    @Operation(summary = "获取实时动态", description = "获取系统最新的用户活动动态，包括登录、注册、点赞、评论等")
    public Result<List<RealtimeActivityDTO>> getRealtimeActivities(
            @Parameter(description = "限制数量", example = "5")
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<RealtimeActivityDTO> activities = analyticsService.getRealtimeActivities(limit);
            return Result.success(activities);
        } catch (Exception e) {
            log.error("获取实时动态失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取实时动态失败"));
        }
    }




    /**
     * 获取待处理事项统计
     */
    @PostMapping("/pending-tasks")
    @Operation(summary = "获取待处理事项统计", description = "获取仪表盘待处理事项的统计数据")
    public Result getPendingTasks() {
        try {
            Map<String, Object> pendingTasks = new HashMap<>();

            // 1. 处理用户举报 - 累加所有待处理的举报（不限时间）
            LambdaQueryWrapper<Report> reportWrapper = new LambdaQueryWrapper<>();
            reportWrapper.eq(Report::getStatus, "pending");
            long pendingReports = reportService.count(reportWrapper);


            // 2. 审核待发布文章 - 统计所有待审核的文章
            LambdaQueryWrapper<AuditRecord> auditWrapper = new LambdaQueryWrapper<>();
            auditWrapper.eq(AuditRecord::getAuditStatus, 0); // 0-待审核
            long pendingAudits = auditRecordService.count(auditWrapper);



            // 3. TODO 可以搞个系统表 系统维护提醒 - 这里可以根据实际需求添加
            Map<String, Object> systemTask = new HashMap<>();
            systemTask.put("title", "系统维护提醒");
            systemTask.put("description", "定期数据库清理");
            systemTask.put("count", 0);
            systemTask.put("type", "system");
            systemTask.put("urgent", false);

            pendingTasks.put("reportsCount", pendingReports);
            pendingTasks.put("auditsCount", pendingAudits);
            pendingTasks.put("system", systemTask);

            // 总待处理数量
            pendingTasks.put("totalCount", pendingReports + pendingAudits);

            return Result.success(pendingTasks);
        } catch (Exception e) {
            log.error("获取待处理事项统计失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取统计数据失败"));
        }
    }

    /**
     * 导出 Excel 报告
     */
    @GetMapping("/export/excel")
    @Operation(summary = "导出 Excel 报告", description = "导出包含概览、趋势、排行、分类等多 Sheet 的 Excel 报告")
    public void exportExcel(HttpServletResponse response) {
        try {
            analyticsExportService.exportExcel(response);
        } catch (Exception e) {
            log.error("导出 Excel 失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 导出 PDF 报告
     */
    @GetMapping("/export/pdf")
    @Operation(summary = "导出 PDF 报告", description = "导出包含概览、趋势、排行等数据的 PDF 报告")
    public void exportPdf(HttpServletResponse response) {
        try {
            analyticsExportService.exportPdf(response);
        } catch (Exception e) {
            log.error("导出 PDF 失败: {}", e.getMessage(), e);
        }
    }

}