package com.fzg.controller.app;

import cn.dev33.satoken.stp.StpUtil;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.service.ReportReasonService;
import com.fzg.service.ReportService;
import com.fzg.vo.ReportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端举报控制器
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户端举报管理", description = "用户举报相关接口")
public class ReportController {

    private final ReportService reportService;
    private final ReportReasonService reportReasonService;

    /**
     * 提交举报
     */
    @PostMapping("/submit")
    @Operation(summary = "提交举报", description = "用户提交举报内容")
    public Result submitReport(@RequestBody ReportRequest reportRequest) {
        try {
            // 获取当前登录用户ID
            String loginId = (String) StpUtil.getLoginId();
            Long userId = Long.valueOf(loginId);
            
            reportRequest.setReporterId(userId);
            
            return reportService.submitReport(reportRequest);
        } catch (Exception e) {
            log.error("提交举报失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("提交举报失败"));
        }
    }

    /**
     * 获取举报原因列表
     */
    @GetMapping("/reasons")
    @Operation(summary = "获取举报原因", description = "根据目标类型获取可用的举报原因")
    public Result getReportReasons(
            @Parameter(description = "目标类型：article-文章, comment-评论, user-用户", example = "article")
            @RequestParam String targetType) {
        return reportReasonService.getReasonsByTargetType(targetType);
    }

    /**
     * 获取所有举报原因
     */
    @GetMapping("/reasons/all")
    @Operation(summary = "获取所有举报原因", description = "获取所有启用的举报原因")
    public Result getAllReportReasons() {
        return reportReasonService.getAllActiveReasons();
    }

    /**
     * 检查是否已举报
     */
    @GetMapping("/check")
    @Operation(summary = "检查是否已举报", description = "检查用户是否已经举报过该内容")
    public Result checkReported(
            @Parameter(description = "目标类型", example = "article")
            @RequestParam String targetType,
            @Parameter(description = "目标ID", example = "1")
            @RequestParam Long targetId) {
        try {
            String loginId = (String) StpUtil.getLoginId();
            Long userId = Long.valueOf(loginId);
            
            boolean hasReported = reportService.hasUserReported(userId, targetType, targetId);
            return Result.success(hasReported);
        } catch (Exception e) {
            log.error("检查举报状态失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("检查举报状态失败"));
        }
    }

    /**
     * 获取我的举报历史
     */
    @GetMapping("/my-reports")
    @Operation(summary = "获取我的举报历史", description = "获取当前用户的举报历史记录")
    public Result getMyReports(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页面大小", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            String loginId = (String) StpUtil.getLoginId();
            Long userId = Long.valueOf(loginId);
            
            return reportService.getUserReportHistory(userId, pageNum, pageSize);
        } catch (Exception e) {
            log.error("获取举报历史失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取举报历史失败"));
        }
    }

    /**
     * 撤销举报
     */
    @DeleteMapping("/{reportId}")
    @Operation(summary = "撤销举报", description = "撤销自己提交的举报")
    public Result cancelReport(
            @Parameter(description = "举报ID", example = "1")
            @PathVariable Long reportId) {
        try {
            String loginId = (String) StpUtil.getLoginId();
            Long userId = Long.valueOf(loginId);
            
            // 检查举报是否属于当前用户且状态为待处理
            com.fzg.model.Report report = reportService.getById(reportId);
            if (report == null) {
                return Result.fail(EnumReturn.valueOf("举报记录不存在"));
            }
            
            if (!report.getReporterId().equals(userId)) {
                return Result.fail(EnumReturn.valueOf("无权限操作"));
            }
            
            if (!"pending".equals(report.getStatus())) {
                return Result.fail(EnumReturn.valueOf("该举报已处理，无法撤销"));
            }
            
            boolean success = reportService.removeById(reportId);
            if (success) {
                return Result.success("撤销举报成功");
            } else {
                return Result.fail(EnumReturn.valueOf("撤销举报失败"));
            }
        } catch (Exception e) {
            log.error("撤销举报失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("撤销举报失败"));
        }
    }
}