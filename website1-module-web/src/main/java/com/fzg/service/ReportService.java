package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Report;
import com.fzg.model.Result;
import com.fzg.vo.ReportQueryRequest;
import com.fzg.vo.ReportRequest;

import java.util.List;

public interface ReportService extends IService<Report> {

    Result submitReport(ReportRequest reportRequest);

    boolean hasUserReported(Long reporterId, String targetType, Long targetId);

    Result getUserReportHistory(Long reporterId, Integer pageNum, Integer pageSize);

    /** 条件查询举报列表（首页默认 pending，支持全条件筛选） */
    Result getReportListByCondition(ReportQueryRequest request);

    /** 统计卡片数据：待处理、紧急、今日已处理、处理率 */
    Result getReportStatistics();

    /** 最近处理记录 */
    Result getRecentProcessed(Integer limit);

    /** 批量处理举报（通过/驳回） */
    Result batchProcessReport(List<Long> reportIds, Long adminId, String status, String remark);

    /** 查看举报目标详情（文章/评论/用户） */
    Result getReportTarget(Long reportId);

    /**
     * 处理单条举报（四种操作）
     * action: delete_content / warn_user / ban_user / reject
     */
    Result processReportAction(Long reportId, Long adminId, String action, String remark);
}
