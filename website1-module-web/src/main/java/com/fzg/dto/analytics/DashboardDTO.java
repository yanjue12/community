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
        // 本月数据
        private Long currentMonthUsers;      // 本月新增用户数
        private Long currentMonthArticles;   // 本月新增文章数
        private Long currentMonthComments;   // 本月新增评论数
        private Long currentMonthViews;      // 本月浏览量
        
        // 上月数据
        private Long lastMonthUsers;         // 上月新增用户数
        private Long lastMonthArticles;      // 上月新增文章数
        private Long lastMonthComments;      // 上月新增评论数
        private Long lastMonthViews;         // 上月浏览量
        
        // 增长比例 (百分比)
        private BigDecimal userGrowthRate;     // 用户增长率
        private BigDecimal articleGrowthRate;  // 文章增长率
        private BigDecimal commentGrowthRate;  // 评论增长率
        private BigDecimal viewGrowthRate;     // 浏览量增长率
        
        // 总计数据
        private Long totalUsers;        // 总用户数
        private Long totalArticles;     // 总文章数
        private Long totalComments;     // 总评论数
        private Long totalViews;        // 总浏览量
        
        // 今日数据
        private Long todayUsers;        // 今日新增用户
        private Long todayArticles;     // 今日新增文章
        private Long todayComments;     // 今日新增评论
        private Long todayViews;        // 今日浏览量
        
        // 实时数据
        private Long onlineUsers;       // 在线用户数
    }
}