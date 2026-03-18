package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.ReportReason;
import com.fzg.model.Result;

/**
 * @description 针对表【report_reason(举报原因配置表)】的数据库操作Service
 */
public interface ReportReasonService extends IService<ReportReason> {

    /**
     * 根据目标类型获取可用的举报原因
     * @param targetType 目标类型
     * @return 举报原因列表
     */
    Result getReasonsByTargetType(String targetType);

    /**
     * 获取所有启用的举报原因
     * @return 举报原因列表
     */
    Result getAllActiveReasons();
}