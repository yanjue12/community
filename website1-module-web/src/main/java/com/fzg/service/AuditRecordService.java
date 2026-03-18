package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Article;
import com.fzg.model.AuditRecord;
import com.fzg.model.Result;
import com.fzg.vo.AuditQueryRequest;

/**
 * @description 针对表【audit_record(审核表)】的数据库操作Service
 */
public interface AuditRecordService extends IService<AuditRecord> {

    /**
     * 获取待审核列表
     * @param auditStatus 审核状态：0-待审核, 1-通过, 2-拒绝
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 审核列表
     */
    Result getPendingAuditList(Byte auditStatus, Integer pageNum, Integer pageSize);

    /**
     * 多条件查询审核列表
     * @param request 查询条件
     * @return 审核列表
     */
    Result getAuditListByCondition(AuditQueryRequest request);

    /**
     * 审核通过
     * @param auditId 审核记录ID
     * @param auditorId 审核人ID
     * @param reason 审核备注
     * @return 审核结果
     */
    Result approveAudit(Long auditId, Long auditorId, String reason);

    /**
     * 审核拒绝
     * @param auditId 审核记录ID
     * @param auditorId 审核人ID
     * @param reason 拒绝原因
     * @return 审核结果
     */
    Result rejectAudit(Long auditId, Long auditorId, String reason);

    /**
     * 批量审核
     * @param auditIds 审核记录ID列表
     * @param auditorId 审核人ID
     * @param auditStatus 审核状态：1-通过, 2-拒绝
     * @param reason 审核备注
     * @return 审核结果
     */
    Result batchAudit(java.util.List<Long> auditIds, Long auditorId, Byte auditStatus, String reason);

    /**
     * 获取审核历史
     * @param auditStatus 审核状态
     * @param auditorId 审核人ID（可选）
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 审核历史
     */
    Result getAuditHistory(Byte auditStatus, Long auditorId, Integer pageNum, Integer pageSize);

    /**
     * 创建审核记录
     * @param articleId 文章ID
     * @return 审核记录
     */
    AuditRecord createAuditRecord(Long articleId);

    /**
     * 根据文章ID获取审核记录
     * @param articleId 文章ID
     * @return 审核记录
     */
    AuditRecord getByArticleId(Long articleId);

    /**
     * 获取审核统计数据
     * @return 统计数据
     */
    Result getAuditStatistics();

    /**
     * 获取最近审核记录
     * @param limit 限制数量，默认3条
     * @return 最近审核记录
     */
    Result getRecentAuditRecords(Integer limit);


}