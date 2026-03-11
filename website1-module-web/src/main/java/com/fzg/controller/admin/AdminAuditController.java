package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.mapper.AuditRecordMapper;
import com.fzg.model.AuditRecord;
import com.fzg.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-审核管理
 */
@RestController
@RequestMapping("/admin/audit")
@SaCheckLogin
public class AdminAuditController {

    @Autowired
    private AuditRecordMapper auditRecordMapper;

    /**
     * 分页查询审核记录
     */
    @GetMapping("/list")
    public Result listAuditRecords(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(required = false) Integer auditStatus,
                                   @RequestParam(required = false) Long articleId) {
        Page<AuditRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        if (auditStatus != null) {
            wrapper.eq(AuditRecord::getAuditStatus, auditStatus);
        }
        if (articleId != null) {
            wrapper.eq(AuditRecord::getArticleId, articleId);
        }
        wrapper.orderByDesc(AuditRecord::getUpdatedAt);
        return Result.success(auditRecordMapper.selectPage(page, wrapper));
    }

    /**
     * 获取审核详情
     */
    @GetMapping("/{id}")
    public Result getAuditRecord(@PathVariable Long id) {
        AuditRecord record = auditRecordMapper.selectById(id);
        return record != null ? Result.success(record) : Result.fail(404, "审核记录不存在");
    }

    /**
     * 获取待审核数量
     */
    @GetMapping("/pending/count")
    public Result getPendingCount() {
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditRecord::getAuditStatus, 0); // 0-待审核
        Long count = auditRecordMapper.selectCount(wrapper);
        return Result.success(count);
    }

    /**
     * 删除审核记录
     */
    @DeleteMapping("/{id}")
    public Result deleteAuditRecord(@PathVariable Long id) {
        int result = auditRecordMapper.deleteById(id);
        return Result.handle(result > 0);
    }

    /**
     * 批量删除审核记录
     */
    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody java.util.List<Long> ids) {
        int result = auditRecordMapper.deleteBatchIds(ids);
        return Result.handle(result > 0);
    }
}
