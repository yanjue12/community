package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.ReportReason;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportReasonMapper extends BaseMapper<ReportReason> {

    /**
     * 根据目标类型获取可用的举报原因
     * @param targetType 目标类型
     * @return 举报原因列表
     */
    List<ReportReason> getReasonsByTargetType(@Param("targetType") String targetType);
}