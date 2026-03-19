package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.AuditRecordMapper;
import com.fzg.mapper.Articlemapper;
import com.fzg.model.Article;
import com.fzg.model.AuditRecord;
import com.fzg.model.Result;
import com.fzg.service.AuditRecordService;
import com.fzg.vo.AuditQueryRequest;
import com.fzg.vo.AuditRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @description 针对表【audit_record(审核表)】的数据库操作Service实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditRecordServiceImpl extends ServiceImpl<AuditRecordMapper, AuditRecord> implements AuditRecordService {

    private final AuditRecordMapper auditRecordMapper;
    private final Articlemapper articleMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AUDIT_LOCK_PREFIX = "audit:lock:";

    @Override
    public Result getPendingAuditList(Byte auditStatus, Integer pageNum, Integer pageSize) {
        try {
            pageNum = pageNum == null ? 1 : pageNum;
            pageSize = pageSize == null ? 10 : pageSize;
            int offset = (pageNum - 1) * pageSize;

            List<AuditRecordVO> auditList = auditRecordMapper.queryPendingAuditList(auditStatus, pageSize, offset);
            Long total = auditRecordMapper.countPendingAudit(auditStatus);

            // 处理状态文本
            auditList.forEach(this::processAuditRecordVO);

            Map<String, Object> result = new HashMap<>();
            result.put("list", auditList);
            result.put("total", total);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取审核列表失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取审核列表失败"));
        }
    }

    @Override
    public Result getAuditListByCondition(AuditQueryRequest request) {
        try {
            // 设置默认值
            Integer pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
            Integer pageSize = request.getPageSize() == null ? 10 : request.getPageSize();
            int offset = (pageNum- 1) * pageSize;

            List<AuditRecordVO> auditList = auditRecordMapper.queryAuditListByCondition(request, offset);
            Long total = auditRecordMapper.countAuditListByCondition(request);

            // 处理状态文本
            auditList.forEach(this::processAuditRecordVO);

            Map<String, Object> result = new HashMap<>();
            result.put("list", auditList);
            result.put("total", total);

            return Result.success(result);
        } catch (Exception e) {
            log.error("多条件查询审核列表失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("查询审核列表失败"));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result approveAudit(Long auditId, Long auditorId, String reason) {
        String lockKey = AUDIT_LOCK_PREFIX + auditId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, auditorId.toString(), 30, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return Result.fail(EnumReturn.valueOf("该审核记录正在处理中，请稍后重试"));
        }
        try {
            AuditRecord auditRecord = this.getById(auditId);
            if (auditRecord == null) {
                return Result.fail(EnumReturn.valueOf("审核记录不存在"));
            }

            if (auditRecord.getAuditStatus() != 0) {
                return Result.fail(EnumReturn.valueOf("该记录已审核，无法重复操作"));
            }

            // 更新审核记录
            auditRecord.setAuditStatus((byte) 1);
            auditRecord.setAuditorId(auditorId);
            auditRecord.setReason(reason);
            auditRecord.setUpdatedAt(new Date());

            boolean updateSuccess = this.updateById(auditRecord);
            if (!updateSuccess) {
                return Result.fail(EnumReturn.valueOf("更新审核记录失败"));
            }

            // 更新文章状态为已发布
            Article article = articleMapper.selectById(auditRecord.getArticleId());
            if (article != null) {
                article.setStatus("1"); // 1-已发布
                article.setPublishedAt(new Date());
                article.setUpdatedAt(new Date());
                articleMapper.updateById(article);
            }

            log.info("审核通过成功: auditId={}, auditorId={}", auditId, auditorId);
            return Result.success("审核通过成功");
        } catch (Exception e) {
            log.error("审核通过失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("审核通过失败"));
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result rejectAudit(Long auditId, Long auditorId, String reason) {
        if (!StringUtils.hasText(reason)) {
            return Result.fail(EnumReturn.valueOf("拒绝原因不能为空"));
        }
        String lockKey = AUDIT_LOCK_PREFIX + auditId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, auditorId.toString(), 30, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return Result.fail(EnumReturn.valueOf("该审核记录正在处理中，请稍后重试"));
        }
        try {
            AuditRecord auditRecord = this.getById(auditId);
            if (auditRecord == null) {
                return Result.fail(EnumReturn.valueOf("审核记录不存在"));
            }

            if (auditRecord.getAuditStatus() != 0) {
                return Result.fail(EnumReturn.valueOf("该记录已审核，无法重复操作"));
            }

            // 更新审核记录
            auditRecord.setAuditStatus((byte) 2);
            auditRecord.setAuditorId(auditorId);
            auditRecord.setReason(reason);
            auditRecord.setUpdatedAt(new Date());

            boolean updateSuccess = this.updateById(auditRecord);
            if (!updateSuccess) {
                return Result.fail(EnumReturn.valueOf("更新审核记录失败"));
            }

            // 更新文章状态为审核拒绝
            Article article = articleMapper.selectById(auditRecord.getArticleId());
            if (article != null) {
                article.setStatus("2"); // 2-审核拒绝
                article.setUpdatedAt(new Date());
                articleMapper.updateById(article);
            }

            log.info("审核拒绝成功: auditId={}, auditorId={}, reason={}", auditId, auditorId, reason);
            return Result.success("审核拒绝成功");
        } catch (Exception e) {
            log.error("审核拒绝失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("审核拒绝失败"));
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result batchAudit(List<Long> auditIds, Long auditorId, Byte auditStatus, String reason) {
        try {
            if (auditIds == null || auditIds.isEmpty()) {
                return Result.fail(EnumReturn.valueOf("审核ID列表不能为空"));
            }

            if (auditStatus == 2 && !StringUtils.hasText(reason)) {
                return Result.fail(EnumReturn.valueOf("批量拒绝时原因不能为空"));
            }

            int successCount = 0;
            for (Long auditId : auditIds) {
                Result result;
                if (auditStatus == 1) {
                    result = approveAudit(auditId, auditorId, reason);
                } else {
                    result = rejectAudit(auditId, auditorId, reason);
                }
                
                if (result.getCode() == 200) {
                    successCount++;
                }
            }

            return Result.success("批量审核完成，成功处理" + successCount + "条记录");
        } catch (Exception e) {
            log.error("批量审核失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("批量审核失败"));
        }
    }

    @Override
    public Result getAuditHistory(Byte auditStatus, Long auditorId, Integer pageNum, Integer pageSize) {
        try {
            pageNum = pageNum == null ? 1 : pageNum;
            pageSize = pageSize == null ? 10 : pageSize;
            int offset = (pageNum - 1) * pageSize;

            List<AuditRecordVO> auditList = auditRecordMapper.queryAuditHistory(auditStatus, auditorId, pageSize, offset);
            Long total = auditRecordMapper.countAuditHistory(auditStatus, auditorId);

            // 处理状态文本
            auditList.forEach(this::processAuditRecordVO);

            Map<String, Object> result = new HashMap<>();
            result.put("list", auditList);
            result.put("total", total);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取审核历史失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取审核历史失败"));
        }
    }

    @Override
    public AuditRecord createAuditRecord(Long articleId) {
        try {
            // 检查是否已存在审核记录
            AuditRecord existingRecord = getByArticleId(articleId);
            if (existingRecord != null) {
                return existingRecord;
            }

            AuditRecord auditRecord = new AuditRecord();
            auditRecord.setBizType("ARTICLE");
            auditRecord.setArticleId(articleId);
            auditRecord.setAuditStatus((byte) 0); // 0-待审核
            auditRecord.setAuditType((byte) 2);
            auditRecord.setCreatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
            auditRecord.setUpdatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));

            this.save(auditRecord);
            return auditRecord;
        } catch (Exception e) {
            log.error("创建审核记录失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public AuditRecord getByArticleId(Long articleId) {
        return auditRecordMapper.getByArticleId(articleId);
    }

    @Override
    public Result getAuditStatistics() {
        try {
            // 待审核数量
            LambdaQueryWrapper<AuditRecord> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.eq(AuditRecord::getAuditStatus, 0);
            long pendingCount = this.count(pendingWrapper);

            // 今日审核数量
            LambdaQueryWrapper<AuditRecord> todayWrapper = new LambdaQueryWrapper<>();
            todayWrapper.ge(AuditRecord::getUpdatedAt, getTodayStart())
                       .in(AuditRecord::getAuditStatus, 1, 2);
            long todayCount = this.count(todayWrapper);

            // 总审核数量
            long totalCount = this.count();

            // 已通过数量
            LambdaQueryWrapper<AuditRecord> approvedWrapper = new LambdaQueryWrapper<>();
            approvedWrapper.eq(AuditRecord::getAuditStatus, 1);
            long approvedCount = this.count(approvedWrapper);

            // 已拒绝数量
            LambdaQueryWrapper<AuditRecord> rejectedWrapper = new LambdaQueryWrapper<>();
            rejectedWrapper.eq(AuditRecord::getAuditStatus, 2);
            long rejectedCount = this.count(rejectedWrapper);

            // 计算通过率
            double approveRate = totalCount > 0 ? (double) approvedCount / totalCount * 100 : 0;

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("pendingCount", pendingCount);
            statistics.put("todayCount", todayCount);
            statistics.put("totalCount", totalCount);
            statistics.put("approvedCount", approvedCount);
            statistics.put("rejectedCount", rejectedCount);
            statistics.put("approveRate", String.format("%.1f%%", approveRate));

            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取审核统计失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取审核统计失败"));
        }
    }

    /**
     * 处理审核记录VO的状态文本
     */
    private void processAuditRecordVO(AuditRecordVO vo) {
        // 审核状态文本
        switch (vo.getAuditStatus()) {
            case 0:
                vo.setAuditStatusText("待审核");
                break;
            case 1:
                vo.setAuditStatusText("已通过");
                break;
            case 2:
                vo.setAuditStatusText("已拒绝");
                break;
            default:
                vo.setAuditStatusText("未知");
        }

        // 审核类型文本
        switch (vo.getAuditType()) {
            case 1:
                vo.setAuditTypeText("自动审核");
                break;
            case 2:
                vo.setAuditTypeText("人工审核");
                break;
            default:
                vo.setAuditTypeText("未知");
        }
    }

    /**
     * 获取今日开始时间
     */
    private Date getTodayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @Override
    public Result getRecentAuditRecords(Integer limit) {
        try {
            // 默认显示3条记录
            limit = limit == null ? 3 : limit;
            
            List<AuditRecordVO> recentRecords = auditRecordMapper.getRecentAuditRecords(limit);
            
            // 处理状态文本和时间描述
            recentRecords.forEach(record -> {
                processAuditRecordVO(record);
                // 添加时间描述
                if (record.getUpdatedAt() != null) {
                    record.setTimeDescription(getTimeDescription(record.getUpdatedAt()));
                }
            });
            
            return Result.success(recentRecords);
        } catch (Exception e) {
            log.error("获取最近审核记录失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取最近审核记录失败"));
        }
    }



    /**
     * 获取时间描述
     */
    private String getTimeDescription(Date date) {
        if (date == null) return "";
        
        long diff = System.currentTimeMillis() - date.getTime();
        long minutes = diff / (1000 * 60);
        long hours = diff / (1000 * 60 * 60);
        long days = diff / (1000 * 60 * 60 * 24);
        
        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (hours < 24) {
            return hours + "小时前";
        } else if (days < 7) {
            return days + "天前";
        } else {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd HH:mm");
            return sdf.format(date);
        }
    }
}