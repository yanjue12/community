package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.service.ReportService;
import com.fzg.vo.ReportQueryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端举报管理
 */
@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
@Slf4j
@SaCheckLogin
@Tag(name = "管理端举报管理")
public class AdminReportController {

    private final ReportService reportService;

    /**
     * 处理单条举报
     * action: delete_content-删除内容, warn_user-警告用户, ban_user-封禁用户, reject-驳回举报
     */
    @PostMapping("/{reportId}/action")
    @Operation(summary = "处理举报", description = "delete_content/warn_user/ban_user/reject")
    public Result processAction(
            @Parameter(description = "举报ID", required = true)
            @PathVariable Long reportId,
            @Parameter(description = "操作类型", required = true)
            @RequestParam String action,
            @Parameter(description = "备注/警告内容")
            @RequestParam(required = false) String remark) {
        try {
            String loginId = (String) StpUtil.getLoginId();
            Long adminId = Long.valueOf(loginId);
            return reportService.processReportAction(reportId, adminId, action, remark);
        } catch (Exception e) {
            log.error("处理举报失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("处理失败"));
        }
    }

    /**
     * 查看举报目标详情（文章/评论/用户）
     */
    @GetMapping("/{reportId}/target")
    @Operation(summary = "查看举报目标", description = "根据举报记录查看对应的文章、评论或用户详情")
    public Result getReportTarget(
            @Parameter(description = "举报ID", required = true)
            @PathVariable Long reportId) {
        return reportService.getReportTarget(reportId);
    }

    /**
     * 统计卡片数据（待处理、紧急、今日已处理、处理率）
     */
    @GetMapping("/statistics")
    @Operation(summary = "统计卡片数据")
    public Result getStatistics() {
        return reportService.getReportStatistics();
    }

    /**
     * 举报列表（首页默认 pending，支持关键词/类型/原因/优先级/状态筛选）
     */
    @PostMapping("/list")
    @Operation(summary = "举报列表查询")
    public Result getReportList(@RequestBody ReportQueryRequest request) {
        if (request == null){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        return reportService.getReportListByCondition(request);
    }

    /**
     * 最近处理记录
     */
    @GetMapping("/recent")
    @Operation(summary = "最近处理记录")
    public Result getRecentProcessed(
            @Parameter(description = "条数，默认2")
            @RequestParam(defaultValue = "2") Integer limit) {
        return reportService.getRecentProcessed(limit);
    }

    /**
     * 批量通过/驳回举报
     * status: resolved-通过, rejected-驳回
     */
    @PostMapping("/batch-process")
    @Operation(summary = "批量处理举报（通过/驳回）")
    public Result batchProcess(
            @Parameter(description = "举报ID列表", required = true)
            @RequestParam List<Long> reportIds,
            @Parameter(description = "resolved-通过, rejected-驳回", required = true)
            @RequestParam String status,
            @Parameter(description = "处理备注")
            @RequestParam(required = false) String remark) {
        try {
            String loginId = (String) StpUtil.getLoginId();
            Long adminId = Long.valueOf(loginId);
            return reportService.batchProcessReport(reportIds, adminId, status, remark);
        } catch (Exception e) {
            log.error("批量处理举报失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("批量处理失败"));
        }
    }
}
