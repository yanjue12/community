package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.Commentmapper;
import com.fzg.mapper.ReportMapper;
import com.fzg.mapper.UserMapper;
import com.fzg.model.Comment;
import com.fzg.model.Report;
import com.fzg.model.Result;
import com.fzg.model.User;
import com.fzg.service.NotificationPublisher;
import com.fzg.service.ReportService;
import com.fzg.vo.ArticleDetailVO;
import com.fzg.vo.RecentReportVO;
import com.fzg.vo.ReportQueryRequest;
import com.fzg.vo.ReportRequest;
import com.fzg.vo.ReportVO;
import com.fzg.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final ReportMapper reportMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Articlemapper articleMapper;
    private final Commentmapper commentMapper;
    private final UserMapper userMapper;
    private final NotificationPublisher notificationPublisher;

    private static final String REPORT_LOCK_PREFIX = "report:lock:";

    // ---- 优先级/状态/类型 文本映射 ----

    private static String priorityText(String reasonType) {
        if (reasonType == null) return "低";
        switch (reasonType) {
            case "harassment": case "copyright": return "高";
            case "inappropriate": return "中";
            default: return "低";
        }
    }

    private static String reasonName(String reasonType) {
        if (reasonType == null) return "其他";
        switch (reasonType) {
            case "spam": return "垃圾信息";
            case "inappropriate": return "不当内容";
            case "harassment": return "骚扰";
            case "copyright": return "版权";
            default: return "其他";
        }
    }

    private static String statusText(String status) {
        if (status == null) return "";
        switch (status) {
            case "pending": return "待处理";
            case "processing": return "处理中";
            case "resolved": return "已处理";
            case "rejected": return "已驳回";
            default: return status;
        }
    }

    private static String targetTypeText(String targetType) {
        if (targetType == null) return "";
        switch (targetType) {
            case "article": return "文章";
            case "comment": return "评论";
            case "user": return "用户";
            default: return targetType;
        }
    }

    private void fillTexts(ReportVO vo) {
        vo.setReasonName(reasonName(vo.getReasonType()));
        vo.setStatusText(statusText(vo.getStatus()));
        vo.setPriority(vo.getPriority()); // already set by SQL
    }

    private void fillRecentTexts(RecentReportVO vo) {
        vo.setStatusText(statusText(vo.getStatus()));
        vo.setTargetTypeText(targetTypeText(vo.getTargetType()));
        vo.setReasonName(reasonName(vo.getReasonType()));
    }

    // ---- 接口实现 ----

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result submitReport(ReportRequest req) {
        try {
            if (req == null || req.getReporterId() == null || req.getTargetId() == null
                    || !StringUtils.hasText(req.getTargetType()) || !StringUtils.hasText(req.getReasonType())) {
                return Result.fail(EnumReturn.valueOf("举报参数不完整"));
            }
            if (hasUserReported(req.getReporterId(), req.getTargetType(), req.getTargetId())) {
                return Result.fail(EnumReturn.valueOf("您已经举报过该内容"));
            }
            Report report = new Report();
            report.setReporterId(req.getReporterId());
            report.setTargetType(req.getTargetType());
            report.setTargetId(req.getTargetId());
            report.setTargetUserId(req.getTargetUserId());
            report.setReasonType(req.getReasonType());
            report.setReasonDetail(req.getReasonDetail());
            report.setEvidenceUrls(req.getEvidenceUrls());
            report.setStatus("pending");
            report.setCreatedAt(new Date());
            report.setUpdatedAt(new Date());
            this.save(report);
            return Result.success("举报提交成功，我们会尽快处理");
        } catch (Exception e) {
            log.error("提交举报失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("举报提交失败"));
        }
    }

    @Override
    public boolean hasUserReported(Long reporterId, String targetType, Long targetId) {
        try {
            return reportMapper.checkUserReported(reporterId, targetType, targetId) > 0;
        } catch (Exception e) {
            log.error("检查用户举报状态失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Result getUserReportHistory(Long reporterId, Integer pageNum, Integer pageSize) {
        try {
            pageNum = pageNum == null ? 1 : pageNum;
            pageSize = pageSize == null ? 10 : pageSize;
            int offset = (pageNum - 1) * pageSize;
            List<Report> reports = reportMapper.getUserReportHistory(reporterId, pageSize, offset);
            long total = this.count(new LambdaQueryWrapper<Report>().eq(Report::getReporterId, reporterId));
            Map<String, Object> data = new HashMap<>();
            data.put("list", reports);
            data.put("total", total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取用户举报历史失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取举报历史失败"));
        }
    }

    @Override
    public Result getReportListByCondition(ReportQueryRequest request) {
        try {
            if (request == null) request = new ReportQueryRequest();
            int pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
            int pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
            request.setPageNum(pageNum);
            request.setPageSize(pageSize);
            int offset = (pageNum - 1) * pageSize;

            List<ReportVO> list = reportMapper.queryReportListByCondition(request, offset);
            Long total = reportMapper.countReportListByCondition(request);
            list.forEach(this::fillTexts);

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询举报列表失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("查询举报列表失败"));
        }
    }

    @Override
    public Result getReportStatistics() {
        try {
            long pending    = orZero(reportMapper.countByStatus("pending"));
            long processing = orZero(reportMapper.countByStatus("processing"));
            long resolved   = orZero(reportMapper.countByStatus("resolved"));
            long rejected   = orZero(reportMapper.countByStatus("rejected"));
            long todayCount = orZero(reportMapper.countTodayReports());
            long total      = orZero(reportMapper.countTotalReports());

            // 紧急 = pending 中 harassment/copyright 类型
            ReportQueryRequest urgentReq = new ReportQueryRequest();
            urgentReq.setStatus("pending");
            urgentReq.setPriority("high");
            urgentReq.setPageSize(1);
            long urgent = orZero(reportMapper.countReportListByCondition(urgentReq));

            // 处理率 = (resolved + rejected) / total
            String processRate = "0%";
            if (total > 0) {
                double rate = (resolved + rejected) * 100.0 / total;
                processRate = String.format("%.1f%%", rate);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("pendingCount", pending);
            data.put("urgentCount", urgent);
            data.put("todayResolvedCount", todayCount);
            data.put("processRate", processRate);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取举报统计失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取统计数据失败"));
        }
    }

    @Override
    public Result getRecentProcessed(Integer limit) {
        try {
            limit = (limit == null || limit <= 0) ? 2 : limit;
            List<RecentReportVO> list = reportMapper.queryRecentProcessed(limit);
            list.forEach(this::fillRecentTexts);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取最近处理记录失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取最近处理记录失败"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result batchProcessReport(List<Long> reportIds, Long adminId, String status, String remark) {
        try {
            if (reportIds == null || reportIds.isEmpty()) {
                return Result.fail(EnumReturn.valueOf("举报ID列表不能为空"));
            }
            if (!"resolved".equals(status) && !"rejected".equals(status)) {
                return Result.fail(EnumReturn.valueOf("处理状态只能为 resolved 或 rejected"));
            }
            int success = 0;
            int skipped = 0;
            Date now = new Date();
            for (Long reportId : reportIds) {
                String lockKey = REPORT_LOCK_PREFIX + reportId;
                Boolean locked = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, adminId.toString(), 30, TimeUnit.SECONDS);
                if (Boolean.FALSE.equals(locked)) {
                    skipped++;
                    log.warn("举报{}正在被其他管理员处理，跳过", reportId);
                    continue;
                }
                try {
                    Report report = this.getById(reportId);
                    if (report == null) continue;
                    if ("resolved".equals(report.getStatus()) || "rejected".equals(report.getStatus())) continue;
                    report.setStatus(status);
                    report.setAdminId(adminId);
                    report.setAdminRemark(remark);
                    report.setProcessedAt(now);
                    report.setUpdatedAt(now);
                    if (this.updateById(report)) success++;
                } finally {
                    redisTemplate.delete(lockKey);
                }
            }
            log.info("管理员{}批量处理举报，状态：{}，成功{}条，跳过{}条", adminId, status, success, skipped);
            String msg = "批量处理完成，成功处理 " + success + " 条";
            if (skipped > 0) msg += "，" + skipped + " 条正在被处理中已跳过";
            return Result.success(msg);
        } catch (Exception e) {
            log.error("批量处理举报失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("批量处理失败"));
        }
    }

    private long orZero(Long val) {
        return val == null ? 0L : val;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result processReportAction(Long reportId, Long adminId, String action, String remark) {
        String lockKey = REPORT_LOCK_PREFIX + reportId;
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, adminId.toString(), 30, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return Result.fail(EnumReturn.valueOf("该举报正在被处理中，请稍后重试"));
        }
        try {
            Report report = this.getById(reportId);
            if (report == null) {
                return Result.fail(EnumReturn.valueOf("举报记录不存在"));
            }
            if ("resolved".equals(report.getStatus()) || "rejected".equals(report.getStatus())) {
                return Result.fail(EnumReturn.valueOf("该举报已处理，无法重复操作"));
            }

            Date now = new Date();
            String targetType = report.getTargetType();
            Long targetId     = report.getTargetId();
            Long targetUserId = report.getTargetUserId();

            switch (action) {
                case "delete_content":
                    doDeleteContent(targetType, targetId);
                    markReport(report, "resolved", adminId, remark, now);
                    log.info("举报{} 删除内容 targetType={} targetId={}", reportId, targetType, targetId);
                    break;

                case "warn_user":
                    doWarnUser(targetUserId, targetType, targetId, remark);
                    markReport(report, "resolved", adminId, remark, now);
                    log.info("举报{} 警告用户 userId={}", reportId, targetUserId);
                    break;

                case "ban_user":
                    doBanUser(targetUserId);
                    doWarnUser(targetUserId, targetType, targetId, "您的账号因违规行为已被封禁。" + (remark != null ? remark : ""));
                    markReport(report, "resolved", adminId, remark, now);
                    log.info("举报{} 封禁用户 userId={}", reportId, targetUserId);
                    break;

                case "reject":
                    markReport(report, "rejected", adminId, remark, now);
                    log.info("举报{} 驳回", reportId);
                    break;

                default:
                    return Result.fail(EnumReturn.valueOf("不支持的操作类型: " + action));
            }

            return Result.success("处理成功");
        } catch (Exception e) {
            log.error("处理举报失败 reportId={} action={}: {}", reportId, action, e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("处理失败"));
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /** 逻辑删除内容 */
    private void doDeleteContent(String targetType, Long targetId) {
        if ("article".equals(targetType)) {
            com.fzg.model.Article article = articleMapper.selectById(targetId);
            if (article != null) {
                article.setStatus("3"); // 3-审核失败，用户还可以修改
                article.setUpdatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
                articleMapper.updateById(article);
            }
        } else if ("comment".equals(targetType)) {
            commentMapper.logicDeleteById(targetId);
        }
        // user 类型不适用删除内容，前端应限制选项
    }

    /** 发送警告通知给被举报用户 */
    private void doWarnUser(Long targetUserId, String targetType, Long targetId, String remark) {
        String warnMsg = StringUtils.hasText(remark) ? remark : "您的内容因违反社区规范已被举报，请注意言行。";
        notificationPublisher.publishNotification(
                targetUserId,
                null,           // 系统消息，无发送者
                "system",
                "admin_warn",
                "违规警告",
                warnMsg,
                targetType,
                targetId,
                "admin_warn_" + targetUserId + "_" + System.currentTimeMillis(),
                Map.of("targetType", targetType, "targetId", String.valueOf(targetId))
        );
    }

    /** 封禁用户 */
    private void doBanUser(Long targetUserId) {
        User user = userMapper.selectById(targetUserId);
        if (user != null) {
            user.setStatus("1"); // 1-禁用
            user.setUpdatedAt(new Date());
            userMapper.updateById(user);
        }
    }

    /** 更新举报记录状态 */
    private void markReport(Report report, String status, Long adminId, String remark, Date now) {
        report.setStatus(status);
        report.setAdminId(adminId);
        report.setAdminRemark(remark);
        report.setProcessedAt(now);
        report.setUpdatedAt(now);
        this.updateById(report);
    }

    @Override
    public Result getReportTarget(Long reportId) {
        try {
            Report report = this.getById(reportId);
            if (report == null) {
                return Result.fail(EnumReturn.valueOf("举报记录不存在"));
            }
            String targetType = report.getTargetType();
            Long targetId = report.getTargetId();

            switch (targetType) {
                case "article": {
                    ArticleDetailVO detail = articleMapper.queryArticleDetails(targetId);
                    if (detail == null) return Result.fail(EnumReturn.valueOf("文章不存在或已删除"));
                    Map<String, Object> data = new HashMap<>();
                    data.put("targetType", "article");
                    data.put("detail", detail);
                    return Result.success(data);
                }
                case "comment": {
                    Comment comment = commentMapper.selectById(targetId);
                    if (comment == null) return Result.fail(EnumReturn.valueOf("评论不存在或已删除"));
                    // 补充评论者信息
                    User commenter = userMapper.selectById(comment.getUserId());
                    Map<String, Object> data = new HashMap<>();
                    data.put("targetType", "comment");
                    data.put("commentId", comment.getId());
                    data.put("content", comment.getContent());
                    data.put("articleId", comment.getArticleId());
                    data.put("likeCount", comment.getLikeCount());
                    data.put("status", comment.getStatus());
                    data.put("createdAt", comment.getCreatedAt());
                    if (commenter != null) {
                        data.put("authorId", commenter.getId());
                        data.put("authorNickname", commenter.getNickname());
                        data.put("authorAvatar", commenter.getAvatar());
                    }
                    return Result.success(data);
                }
                case "user": {
                    User user = userMapper.selectById(targetId);
                    if (user == null) return Result.fail(EnumReturn.valueOf("用户不存在"));
                    UserVO vo = new UserVO();
                    BeanUtils.copyProperties(user, vo);
                    vo.setUserId(String.valueOf(user.getId()));
                    vo.setFollowCount(user.getFollowerCount());
                    vo.setFollowingCount(user.getFollowingCount());
                    Map<String, Object> data = new HashMap<>();
                    data.put("targetType", "user");
                    data.put("detail", vo);
                    return Result.success(data);
                }
                default:
                    return Result.fail(EnumReturn.valueOf("不支持的举报目标类型: " + targetType));
            }
        } catch (Exception e) {
            log.error("查看举报目标失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("查看失败"));
        }
    }
}
