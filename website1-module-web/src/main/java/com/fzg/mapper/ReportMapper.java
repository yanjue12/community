package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Report;
import com.fzg.vo.RecentReportVO;
import com.fzg.vo.ReportQueryRequest;
import com.fzg.vo.ReportVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportMapper extends BaseMapper<Report> {

    int checkUserReported(@Param("reporterId") Long reporterId,
                          @Param("targetType") String targetType,
                          @Param("targetId") Long targetId);

    List<Report> getUserReportHistory(@Param("reporterId") Long reporterId,
                                      @Param("pageSize") Integer pageSize,
                                      @Param("offset") Integer offset);

    /** 条件查询举报列表（含关联信息） */
    List<ReportVO> queryReportListByCondition(@Param("req") ReportQueryRequest req,
                                              @Param("offset") Integer offset);

    Long countReportListByCondition(@Param("req") ReportQueryRequest req);

    /** 统计卡片数据 */
    Long countByStatus(@Param("status") String status);

    Long countTodayReports();

    Long countTotalReports();

    /** 最近处理记录 */
    List<RecentReportVO> queryRecentProcessed(@Param("limit") Integer limit);
}
