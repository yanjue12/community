package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.AuditRecord;
import com.fzg.vo.AuditRecordVO;
import com.fzg.vo.AuditQueryRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {

    /**
     * 查询待审核的文章列表（包含文章详细信息）
     * @param auditStatus 审核状态
     * @param pageSize 页面大小
     * @param offset 偏移量
     * @return 审核记录列表
     */
    List<AuditRecordVO> queryPendingAuditList(@Param("auditStatus") Byte auditStatus,
                                              @Param("pageSize") Integer pageSize,
                                              @Param("offset") Integer offset);

    /**
     * 统计待审核记录数量
     * @param auditStatus 审核状态
     * @return 数量
     */
    Long countPendingAudit(@Param("auditStatus") Byte auditStatus);

    /**
     * 多条件查询审核列表
     * @param request 查询条件
     * @param offset 偏移量
     * @return 审核记录列表
     */
    List<AuditRecordVO> queryAuditListByCondition(@Param("request") AuditQueryRequest request,
                                                  @Param("offset") Integer offset);

    /**
     * 多条件统计审核记录数量
     * @param request 查询条件
     * @return 数量
     */
    Long countAuditListByCondition(@Param("request") AuditQueryRequest request);

    /**
     * 查询审核历史记录
     * @param auditStatus 审核状态
     * @param auditorId 审核人ID
     * @param pageSize 页面大小
     * @param offset 偏移量
     * @return 审核记录列表
     */
    List<AuditRecordVO> queryAuditHistory(@Param("auditStatus") Byte auditStatus,
                                          @Param("auditorId") Long auditorId,
                                          @Param("pageSize") Integer pageSize,
                                          @Param("offset") Integer offset);

    /**
     * 统计审核历史记录数量
     * @param auditStatus 审核状态
     * @param auditorId 审核人ID
     * @return 数量
     */
    Long countAuditHistory(@Param("auditStatus") Byte auditStatus,
                           @Param("auditorId") Long auditorId);

    /**
     * 根据文章ID查询审核记录
     * @param articleId 文章ID
     * @return 审核记录
     */
    AuditRecord getByArticleId(@Param("articleId") Long articleId);

    /**
     * 获取最近审核记录
     * @param limit 限制数量
     * @return 最近审核记录列表
     */
    List<AuditRecordVO> getRecentAuditRecords(@Param("limit") Integer limit);
}