package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Report;
import com.fzg.model.Result;
import com.fzg.vo.ReportRequest;

import java.util.List;

/**
 * @description 针对表【report(举报表)】的数据库操作Service
 */
public interface ReportService extends IService<Report> {

    /**
     * 提交举报
     * @param reportRequest 举报请求
     * @return 结果
     */
    Result submitReport(ReportRequest reportRequest);

    /**
     * 检查用户是否已经举报过该目标
     * @param reporterId 举报人ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 是否已举报
     */
    boolean hasUserReported(Long reporterId, String targetType, Long targetId);

    /**
     * 获取用户的举报历史
     * @param reporterId 举报人ID
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 举报历史列表
     */
    Result getUserReportHistory(Long reporterId, Integer pageNum, Integer pageSize);

    /**
     * 管理端查询举报列表
     * @param status 状态
     * @param targetType 目标类型
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 举报列表
     */
    Result getReportList(String status, String targetType, Integer pageNum, Integer pageSize);

    /**
     * 管理员处理举报
     * @param reportId 举报ID
     * @param adminId 管理员ID
     * @param status 处理状态
     * @param remark 处理备注
     * @return 处理结果
     */
    Result processReport(Long reportId, Long adminId, String status, String remark);

    /**
     * 批量处理举报
     * @param reportIds 举报ID列表
     * @param adminId 管理员ID
     * @param status 处理状态
     * @param remark 处理备注
     * @return 处理结果
     */
    Result batchProcessReport(List<Long> reportIds, Long adminId, String status, String remark);
}