package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;

import com.fzg.enums.EnumReturn;
import com.fzg.model.AuditRecord;
import com.fzg.model.Result;
import com.fzg.service.AuditRecordService;
import com.fzg.vo.AuditQueryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端审核控制器
 */
@RestController
@RequestMapping("/admin/audit")
@RequiredArgsConstructor
@Slf4j
@SaCheckLogin
@SaCheckRole(value = {"admin", "auditAdmin", "reportAdmin"}, mode = SaMode.OR)
@Tag(name = "管理端审核管理", description = "管理员审核相关接口")
public class AdminAuditController {

    private final AuditRecordService auditRecordService;



    /**
     * 多条件查询审核列表
     */
    @PostMapping("/list")
    @Operation(summary = "多条件查询审核列表", description = "支持文章标题、作者名、审核状态、分类等多条件筛选")
    public Result getAuditListByCondition(@RequestBody AuditQueryRequest request) {
        if(null == request){
            return Result.fail(EnumReturn.REQUSET_IS_EMPTY);
        }
        return auditRecordService.getAuditListByCondition(request);
    }





    /**
     * 审核通过
     */
    @PostMapping("/{auditId}/approve")
    @SaCheckRole(value = {"admin", "auditAdmin"}, mode = SaMode.OR)
    @Operation(summary = "审核通过", description = "管理员审核通过文章")
    public Result approveAudit(
            @Parameter(description = "审核记录ID", example = "1")
            @PathVariable Long auditId,
            @Parameter(description = "审核备注")
            @RequestParam(required = false) String reason) {
        try {
            String loginId = (String) StpUtil.getLoginId();
            Long auditorId = Long.valueOf(loginId);
            
            return auditRecordService.approveAudit(auditId, auditorId, reason);
        } catch (Exception e) {
            log.error("审核通过失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("审核通过失败"));
        }
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/{auditId}/reject")
    @SaCheckRole(value = {"admin", "auditAdmin"}, mode = SaMode.OR)
    @Operation(summary = "审核拒绝", description = "管理员审核拒绝文章")
    public Result rejectAudit(
            @Parameter(description = "审核记录ID", example = "1")
            @PathVariable Long auditId,
            @Parameter(description = "拒绝原因", required = true)
            @RequestParam String reason) {
        try {
            String loginId = (String) StpUtil.getLoginId();
            Long auditorId = Long.valueOf(loginId);
            
            return auditRecordService.rejectAudit(auditId, auditorId, reason);
        } catch (Exception e) {
            log.error("审核拒绝失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("审核拒绝失败"));
        }
    }

    /**
     * 批量审核
     */
    @PostMapping("/batch")
    @SaCheckRole(value = {"admin", "auditAdmin"}, mode = SaMode.OR)
    @Operation(summary = "批量审核", description = "批量审核多个文章")
    public Result batchAudit(
            @Parameter(description = "审核记录ID列表", required = true)
            @RequestParam List<Long> auditIds,
            @Parameter(description = "审核状态：1-通过, 2-拒绝", required = true)
            @RequestParam Byte auditStatus,
            @Parameter(description = "审核备注")
            @RequestParam(required = false) String reason) {
        try {
            String loginId = (String) StpUtil.getLoginId();
            Long auditorId = Long.valueOf(loginId);
            
            return auditRecordService.batchAudit(auditIds, auditorId, auditStatus, reason);
        } catch (Exception e) {
            log.error("批量审核失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("批量审核失败"));
        }
    }

    /**
     * 获取审核历史
     */
    @GetMapping("/history")
    @Operation(summary = "获取审核历史", description = "获取审核历史记录")
    public Result getAuditHistory(
            @Parameter(description = "审核状态：1-已通过, 2-已拒绝")
            @RequestParam(required = false) Byte auditStatus,
            @Parameter(description = "审核人ID（不传则查看所有人的审核历史）")
            @RequestParam(required = false) Long auditorId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return auditRecordService.getAuditHistory(auditStatus, auditorId, pageNum, pageSize);
    }



    /**
     * 获取审核统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取审核统计", description = "获取审核的统计数据")
    public Result getAuditStatistics() {
        return auditRecordService.getAuditStatistics();
    }



    /**
     * 根据文章ID获取审核记录
     */
    @GetMapping("/article/{articleId}")
    @Operation(summary = "根据文章ID获取审核记录", description = "查看指定文章的审核记录")
    public Result getAuditByArticleId(
            @Parameter(description = "文章ID", example = "1")
            @PathVariable Long articleId) {
        try {
            AuditRecord auditRecord = auditRecordService.getByArticleId(articleId);
            if (auditRecord == null) {
                return Result.fail(EnumReturn.valueOf("该文章暂无审核记录"));
            }
            return Result.success(auditRecord);
        } catch (Exception e) {
            log.error("获取文章审核记录失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取审核记录失败"));
        }
    }

    /**
     * 获取最近审核记录
     */
    @GetMapping("/recent")
    @Operation(summary = "获取最近审核记录", description = "获取最近的审核记录，用于首页展示")
    public Result getRecentAuditRecords(
            @Parameter(description = "限制数量，默认3条", example = "3")
            @RequestParam(defaultValue = "3") Integer limit) {
        return auditRecordService.getRecentAuditRecords(limit);
    }
}