package com.fzg.service;

import com.fzg.dto.analytics.ChartDataDTO;
import com.fzg.dto.analytics.DashboardDTO;
import com.fzg.dto.analytics.RealtimeActivityDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 数据分析服务接口
 */
public interface AnalyticsService {
    
    /**
     * 获取仪表板数据
     */
    DashboardDTO getDashboardData();
    
    /**
     * 获取用户增长趋势（支持多种时间维度）
     * @param timeType 时间类型：today/thisMonth/thisYear/custom
     * @param startDate 开始日期（自定义时间段时使用）
     * @param endDate 结束日期（自定义时间段时使用）
     */
    List<ChartDataDTO.LineItem> getUserGrowthTrend(String timeType, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取文章发布趋势
     * @param days 天数
     */
    List<ChartDataDTO.LineItem> getArticlePublishTrend(int days);

    /**
     * 获取文章发布趋势（支持多种时间维度）
     * @param timeType 时间类型：today/thisMonth/thisYear/custom
     * @param startDate 开始日期（自定义时间段时使用）
     * @param endDate 结束日期（自定义时间段时使用）
     */
    List<ChartDataDTO.LineItem> getArticlePublishTrend(String timeType, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取文章分类分布
     */
    List<ChartDataDTO.PieItem> getCategoryDistribution();
    
    /**
     * 获取标签使用分布
     * @param limit 限制数量
     */
    List<ChartDataDTO.PieItem> getTagDistribution(int limit);
    
    /**
     * 获取热门文章排行
     * @param limit 限制数量
     * @param days 统计天数
     */
    List<ChartDataDTO.RankItem> getHotArticles(int limit, int days);
    
    /**
     * 获取活跃用户排行
     * @param limit 限制数量
     * @param days 统计天数
     */
    List<ChartDataDTO.RankItem> getActiveUsers(int limit, int days);
    
    /**
     * 获取用户增长对比
     * @param period 周期：day/week/month
     */
    ChartDataDTO.TrendData getUserGrowthComparison(String period);
    
    /**
     * 获取文章发布对比
     * @param period 周期：day/week/month
     */
    ChartDataDTO.TrendData getArticlePublishComparison(String period);
    
    /**
     * 获取评论活跃度趋势
     * @param days 天数
     */
    List<ChartDataDTO.LineItem> getCommentActivityTrend(int days);
    
    /**
     * 获取浏览量趋势
     * @param days 天数
     */
    List<ChartDataDTO.LineItem> getViewTrend(int days);
    
    /**
     * 获取实时动态
     * @param limit 限制数量，默认5条
     */
    List<RealtimeActivityDTO> getRealtimeActivities(int limit);
}