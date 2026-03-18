package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.ReportReasonMapper;
import com.fzg.model.ReportReason;
import com.fzg.model.Result;
import com.fzg.service.ReportReasonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @description 针对表【report_reason(举报原因配置表)】的数据库操作Service实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportReasonServiceImpl extends ServiceImpl<ReportReasonMapper, ReportReason> implements ReportReasonService {

    private final ReportReasonMapper reportReasonMapper;

    @Override
    public Result getReasonsByTargetType(String targetType) {
        try {
            if (!StringUtils.hasText(targetType)) {
                return Result.fail(EnumReturn.valueOf("目标类型不能为空"));
            }

            List<ReportReason> reasons = reportReasonMapper.getReasonsByTargetType(targetType);
            return Result.success(reasons);
        } catch (Exception e) {
            log.error("获取举报原因失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取举报原因失败"));
        }
    }

    @Override
    public Result getAllActiveReasons() {
        try {
            LambdaQueryWrapper<ReportReason> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReportReason::getStatus, "active")
                   .orderByAsc(ReportReason::getSort);
            
            List<ReportReason> reasons = this.list(wrapper);
            return Result.success(reasons);
        } catch (Exception e) {
            log.error("获取所有举报原因失败：{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("获取举报原因失败"));
        }
    }
}