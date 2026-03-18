package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Report;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * 检查用户是否已经举报过该目标
     * @param reporterId 举报人ID
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 举报记录数量
     */
    int checkUserReported(@Param("reporterId") Long reporterId, 
                         @Param("targetType") String targetType, 
                         @Param("targetId") Long targetId);

    /**
     * 获取用户的举报历史
     * @param reporterId 举报人ID
     * @param pageSize 页面大小
     * @param offset 偏移量
     * @return 举报记录列表
     */
    List<Report> getUserReportHistory(@Param("reporterId") Long reporterId,
                                     @Param("pageSize") Integer pageSize,
                                     @Param("offset") Integer offset);

    /**
     * 管理端查询举报列表
     * @param status 状态
     * @param targetType 目标类型
     * @param pageSize 页面大小
     * @param offset 偏移量
     * @return 举报记录列表
     */
    List<Report> queryReportList(@Param("status") String status,
                                @Param("targetType") String targetType,
                                @Param("pageSize") Integer pageSize,
                                @Param("offset") Integer offset);

    /**
     * 统计举报数量
     * @param status 状态
     * @param targetType 目标类型
     * @return 数量
     */
    Long countReportList(@Param("status") String status,
                        @Param("targetType") String targetType);
}