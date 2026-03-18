package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.ReportMapper;
import com.fzg.model.Report;
import com.fzg.model.Result;
import com.fzg.service.ReportService;
import com.fzg.vo.ReportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description 针对表【report(举报表)】的数据库操作Service实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final ReportMapper reportMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result submitReport(ReportRequest reportRequest) {
        try {
            // 参数校验
            if (reportRequest == null) {
                return Result.fail(EnumReturn.valueOf("举报参数不能为空"));
            }
            
            if (reportRequest.getReporterId() == null) {
                return Result.fail(EnumReturn.valueOf("举报人ID不能为空"));
            }
            
            if (!StringUtils.hasText(reportRequest.getTargetType())) {
                return Result.fail(EnumReturn.valueOf("举报目标类型不能为空"));
            }
            
            if (reportRequest.getTargetId() == null) {
                return Result.fail(EnumReturn.valueOf("举报目标ID不能为空"));
            }
            
            if (reportRequest.getTargetUserId() == null) {
                return Result.fail(EnumReturn.valueOf("被举报用户ID不能为空"));
            }
            
            if (!StringUtils.hasText(reportRequest.getReasonType())) {
                return Result.fail(EnumReturn.valueOf("举报原因类型不能为空"));
            }

            // 检查是否已经举报过
            if (hasUserReported(reportRequest.getReporterId(), reportRequest.getTargetType(), reportRequest.getTargetId())) {
                return Result.fail(EnumReturn.valueOf("您已经举报过该内容"));
            }

            // 创建举报记录
            Report report = new Report();
            report.setReporterId(reportRequest.getReporterId());
            report.setTargetType(reportRequest.getTargetType());
            report.setTargetId(reportRequest.getTargetId());
            report.setTargetUserId(reportRequest.getTargetUserId());
            report.setReasonType(reportRequest.getReasonType());
            report.setReasonDetail(reportRequest.getReasonDetail());
            report.setEvidenceUrls(reportRequest.getEvidenceUrls());
            report.setStatus("pending");
            report.setCreatedAt(new Date());
            report.setUpdatedAt(new Date());

            boolean success = this.save(report);
            if (success) {
                log.info("用户{}举报{}成功，举报ID：{}", reportRequest.getReporterId(), 
                        reportRequest.getTargetType() + ":" + reportRequest.getTargetId(), report.getId());
                return Result.success("举报提交成功，我们会尽快处理");
            } else {
                return Result.fail(EnumReturn.valueOf("举报提交失败"));
            }
        } catch (Exception e) {
            log.error("提交举报失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("举报提交失败"));
        }
    }

    @Override
    public boolean hasUserReported(Long reporterId, String targetType, Long targetId) {
        try {
            int count = reportMapper.checkUserReported(reporterId, targetType, targetId);
            return count > 0;
        } catch (Exception e) {
            log.error("检查用户举报状态失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Result getUserReportHistory(Long reporterId, Integer pageNum, Integer pageSize) {
        try {
            if (reporterId == null) {
                return Result.fail(EnumReturn.valueOf("用户ID不能为空"));
            }

            pageNum = pageNum == null ? 1 : pageNum;
            pageSize = pageSize == null ? 10 : pageSize;
            int offset = (pageNum - 1) * pageSize;

            List<Report> reports = reportMapper.getUserReportHistory(reporterId, pageSize, offset);
            
            // 查询总数
            LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Report::getReporterId, reporterId);
            long total = this.count(wrapper);

            return Result.success(Map.of("list", reports, "total", total));
        } catch (Exception e) {
            log.error("获取用户举报历史失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取举报历史失败"));
        }
    }

    @Override
    public Result getReportList(String status, String targetType, Integer pageNum, Integer pageSize) {
        try {
            pageNum = pageNum == null ? 1 : pageNum;
            pageSize = pageSize == null ? 10 : pageSize;
            int offset = (pageNum - 1) * pageSize;

            List<Report> reports = reportMapper.queryReportList(status, targetType, pageSize, offset);
            Long total = reportMapper.countReportList(status, targetType);

            return Result.success(Map.of("list", reports, "total", total));
        } catch (Exception e) {
            log.error("获取举报列表失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取举报列表失败"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result processReport(Long reportId, Long adminId, String status, String remark) {
        try {
            if (reportId == null) {
                return Result.fail(EnumReturn.valueOf("举报ID不能为空"));
            }
            
            if (adminId == null) {
                return Result.fail(EnumReturn.valueOf("管理员ID不能为空"));
            }
            
            if (!StringUtils.hasText(status)) {
                return Result.fail(EnumReturn.valueOf("处理状态不能为空"));
            }

            Report report = this.getById(reportId);
            if (report == null) {
                return Result.fail(EnumReturn.valueOf("举报记录不存在"));
            }

            if (!"pending".equals(report.getStatus()) && !"processing".equals(report.getStatus())) {
                return Result.fail(EnumReturn.valueOf("该举报已处理，无法重复处理"));
            }

            // 更新举报状态
            report.setStatus(status);
            report.setAdminId(adminId);
            report.setAdminRemark(remark);
            report.setProcessedAt(new Date());
            report.setUpdatedAt(new Date());

            boolean success = this.updateById(report);
            if (success) {
                log.info("管理员{}处理举报{}成功，状态：{}", adminId, reportId, status);
                return Result.success("处理成功");
            } else {
                return Result.fail(EnumReturn.valueOf("处理失败"));
            }
        } catch (Exception e) {
            log.error("处理举报失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("处理举报失败"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result batchProcessReport(List<Long> reportIds, Long adminId, String status, String remark) {
        try {
            if (reportIds == null || reportIds.isEmpty()) {
                return Result.fail(EnumReturn.valueOf("举报ID列表不能为空"));
            }
            
            if (adminId == null) {
                return Result.fail(EnumReturn.valueOf("管理员ID不能为空"));
            }
            
            if (!StringUtils.hasText(status)) {
                return Result.fail(EnumReturn.valueOf("处理状态不能为空"));
            }

            int successCount = 0;
            for (Long reportId : reportIds) {
                Result result = processReport(reportId, adminId, status, remark);
                if (result.getCode() == 200) {
                    successCount++;
                }
            }

            return Result.success("批量处理完成，成功处理" + successCount + "条记录");
        } catch (Exception e) {
            log.error("批量处理举报失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("批量处理失败"));
        }
    }
}