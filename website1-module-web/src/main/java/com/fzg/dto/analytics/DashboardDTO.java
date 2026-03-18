package com.fzg.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 仪表板数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    
    // 概览数据
    private OverviewData overview;
    
    // 用户增长趋势
    private List<ChartDataDTO.LineItem> userGrowthTrend;
    
    // 文章发布趋势
    private List<ChartDataDTO.LineItem> articlePublishTrend;
    
    // 文章分类分布
    private List<ChartDataDTO.PieItem> categoryDistribution;
    
    // 标签使用分布
    private List<ChartDataDTO.PieItem> tagDistribution;
    
    // 热门文章排行
    private List<ChartDataDTO.RankItem> hotArticles;
    
    // 活跃用户排行
    private List<ChartDataDTO.RankItem> activeUsers;
    
    // 用户增长对比
    private ChartDataDTO.TrendData userGrowthComparison;
    
    // 文章发布对比
    private ChartDataDTO.TrendData articlePublishComparison;
    
    /**
     * 概览数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewData {

        private Long currentMonthUsers;
        private Long currentMonthArticles;
        private Long currentMonthComments;
        private Long currentMonthViews;
        
        // 上月数据
        private Long lastMonthUsers;
        private Long lastMonthArticles;
        private Long lastMonthComments;
        private Long lastMonthViews;
        
        // 增长比例 (百分比) - 本月与上月对比
        private BigDecimal userGrowthRate;
        private BigDecimal articleGrowthRate;
        private BigDecimal commentGrowthRate;
        private BigDecimal viewGrowthRate;
        
        // 总计数据
        private Long totalUsers;
        private Long totalArticles;
        private Long totalComments;
        private Long totalViews;
        
        // 今日数据
        private Long todayUsers;
        private Long todayArticles;
        private Long todayComments;
        private Long todayViews;
        
        // 今日用户活跃度数据（24小时，每小时的登录人数）
        private List<HourlyLoginData> todayLoginActivity;
        
        // 实时数据
        private Long onlineUsers;       // 在线用户数
    }
    
    /**
     * 每小时登录数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyLoginData {
        private Integer hour;           // 小时 (0-23)
        private Long loginCount;        // 该小时的登录人数
        private String timeLabel;       // 时间标签，如 "00:00", "01:00"
    }
}