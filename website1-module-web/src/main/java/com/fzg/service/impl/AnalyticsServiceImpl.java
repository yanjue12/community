package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.dto.analytics.ChartDataDTO;
import com.fzg.dto.analytics.DashboardDTO;
import com.fzg.mapper.*;
import com.fzg.model.*;
import com.fzg.service.AnalyticsService;
import com.fzg.websocket.WebSocketManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据分析服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {
    
    private final UserMapper userMapper;
    private final Articlemapper articleMapper;
    private final Commentmapper commentMapper;
    private final Categorymapper categoryMapper;
    
    @Override
    public DashboardDTO getDashboardData() {

        articleMapper.selectCount(new LambdaQueryWrapper<Article>());

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setOverview(getOverviewData());
        dashboard.setUserGrowthTrend(getUserGrowthTrend(7));
        dashboard.setArticlePublishTrend(getArticlePublishTrend(7));
        dashboard.setCategoryDistribution(getCategoryDistribution());
        dashboard.setTagDistribution(getTagDistribution(10));
        dashboard.setHotArticles(getHotArticles(10, 7));
        dashboard.setActiveUsers(getActiveUsers(10, 7));
        dashboard.setUserGrowthComparison(getUserGrowthComparison("day"));
        dashboard.setArticlePublishComparison(getArticlePublishComparison("day"));
        return dashboard;
    }

    private DashboardDTO.OverviewData getOverviewData() {
        DashboardDTO.OverviewData overview = new DashboardDTO.OverviewData();
        
        // 获取当前时间
        LocalDate now = LocalDate.now();
        LocalDate currentMonthStart = now.withDayOfMonth(1);
        LocalDate currentMonthEnd = now.plusDays(1);
        LocalDate lastMonthStart = currentMonthStart.minusMonths(1);
        LocalDate lastMonthEnd = currentMonthStart;
        
        LocalDateTime todayStart = now.atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        
        // 本月数据统计
        overview.setCurrentMonthUsers(userMapper.selectCount(
            new LambdaQueryWrapper<User>().between(User::getCreatedAt, 
                currentMonthStart.atStartOfDay(), currentMonthEnd.atStartOfDay())));
        
        overview.setCurrentMonthArticles(articleMapper.selectCount(
            new LambdaQueryWrapper<Article>().between(Article::getCreatedAt,
                currentMonthStart.atStartOfDay(), currentMonthEnd.atStartOfDay())));
        
        overview.setCurrentMonthComments(commentMapper.selectCount(
            new LambdaQueryWrapper<Comment>().between(Comment::getCreatedAt, 
                currentMonthStart.atStartOfDay(), currentMonthEnd.atStartOfDay())));
        
        // 上月数据统计
        overview.setLastMonthUsers(userMapper.selectCount(
            new LambdaQueryWrapper<User>().between(User::getCreatedAt, 
                lastMonthStart.atStartOfDay(), lastMonthEnd.atStartOfDay())));
        
        overview.setLastMonthArticles(articleMapper.selectCount(
            new LambdaQueryWrapper<Article>().between(Article::getCreatedAt, 
                lastMonthStart.atStartOfDay(), lastMonthEnd.atStartOfDay())));
        
        overview.setLastMonthComments(commentMapper.selectCount(
            new LambdaQueryWrapper<Comment>().between(Comment::getCreatedAt, 
                lastMonthStart.atStartOfDay(), lastMonthEnd.atStartOfDay())));
        
        // 计算增长率
        overview.setUserGrowthRate(calculateGrowthRate(overview.getCurrentMonthUsers(), overview.getLastMonthUsers()));
        overview.setArticleGrowthRate(calculateGrowthRate(overview.getCurrentMonthArticles(), overview.getLastMonthArticles()));
        overview.setCommentGrowthRate(calculateGrowthRate(overview.getCurrentMonthComments(), overview.getLastMonthComments()));
        
        // 总计数据
        overview.setTotalUsers(userMapper.selectCount(null));
        overview.setTotalArticles(articleMapper.selectCount(null));
        overview.setTotalComments(commentMapper.selectCount(null));
        
        // 今日数据
        overview.setTodayUsers(userMapper.selectCount(
            new LambdaQueryWrapper<User>().between(User::getCreatedAt, todayStart, todayEnd)));
        overview.setTodayArticles(articleMapper.selectCount(
            new LambdaQueryWrapper<Article>().between(Article::getCreatedAt, todayStart, todayEnd)));
        overview.setTodayComments(commentMapper.selectCount(
            new LambdaQueryWrapper<Comment>().between(Comment::getCreatedAt, todayStart, todayEnd)));
        
        // 实时数据
        overview.setOnlineUsers((long) WebSocketManager.getOnlineUserCount());
        
        // 浏览量数据 (如果有浏览记录表的话)
        overview.setCurrentMonthViews(0L);
        overview.setLastMonthViews(0L);
        overview.setTotalViews(0L);
        overview.setTodayViews(0L);
        overview.setViewGrowthRate(BigDecimal.ZERO);
        
        return overview;
    }
    
    /**
     * 计算增长率
     * @param current 当前值
     * @param previous 上期值
     * @return 增长率百分比
     */
    private BigDecimal calculateGrowthRate(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? new BigDecimal("100.00") : BigDecimal.ZERO;
        }
        
        if (current == null) {
            return new BigDecimal("-100.00");
        }
        
        BigDecimal currentBd = new BigDecimal(current);
        BigDecimal previousBd = new BigDecimal(previous);
        BigDecimal difference = currentBd.subtract(previousBd);
        
        return difference.divide(previousBd, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }






    @Override
    public List<ChartDataDTO.LineItem> getUserGrowthTrend(int days) {
        List<ChartDataDTO.LineItem> trend = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().between(User::getCreatedAt, dayStart, dayEnd));

            trend.add(new ChartDataDTO.LineItem(
                    date.format(DateTimeFormatter.ofPattern("MM-dd")), count, "新增用户"));
        }
        return trend;
    }

    @Override
    public List<ChartDataDTO.LineItem> getArticlePublishTrend(int days) {
        List<ChartDataDTO.LineItem> trend = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            Long count = articleMapper.selectCount(
                    new LambdaQueryWrapper<Article>().between(Article::getCreatedAt, dayStart, dayEnd));

            trend.add(new ChartDataDTO.LineItem(
                    date.format(DateTimeFormatter.ofPattern("MM-dd")), count, "发布文章"));
        }
        return trend;
    }

    @Override
    public List<ChartDataDTO.PieItem> getCategoryDistribution() {
        List<Category> categories = categoryMapper.selectList(null);
        List<ChartDataDTO.PieItem> distribution = new ArrayList<>();
        String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#DDA0DD", "#98D8C8", "#F7DC6F"};
        int colorIndex = 0;

        for (Category category : categories) {
            Long count = articleMapper.selectCount(
                    new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, category.getId()));

            if (count > 0) {
                distribution.add(new ChartDataDTO.PieItem(
                        category.getName(), count, colors[colorIndex % colors.length]));
                colorIndex++;
            }
        }
        return distribution;
    }

    @Override
    public List<ChartDataDTO.PieItem> getTagDistribution(int limit) {
        List<ChartDataDTO.PieItem> distribution = new ArrayList<>();
        String[] sampleTags = {"Java", "Spring Boot", "Vue.js", "MySQL", "Redis", "Docker", "微服务", "前端", "后端", "算法"};
        String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#DDA0DD", "#98D8C8", "#F7DC6F", "#85C1E9", "#F8C471"};

        for (int i = 0; i < Math.min(limit, sampleTags.length); i++) {
            distribution.add(new ChartDataDTO.PieItem(
                    sampleTags[i], (long) (Math.random() * 100 + 10), colors[i]));
        }
        return distribution;
    }

    @Override
    public List<ChartDataDTO.RankItem> getHotArticles(int limit, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>()
                        .ge(Article::getCreatedAt, startTime)
                        .orderByDesc(Article::getViewCount)
                        .last("LIMIT " + limit));

        List<ChartDataDTO.RankItem> hotArticles = new ArrayList<>();
        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            hotArticles.add(new ChartDataDTO.RankItem(
                    article.getId(), article.getTitle(), Long.valueOf(article.getViewCount()) , i + 1, "stable"));
        }
        return hotArticles;
    }

    @Override
    public List<ChartDataDTO.RankItem> getActiveUsers(int limit, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().orderByDesc(User::getId).last("LIMIT " + limit));

        List<ChartDataDTO.RankItem> activeUsers = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Long articleCount = articleMapper.selectCount(
                    new LambdaQueryWrapper<Article>()
                            .eq(Article::getUserId, user.getId())
                            .ge(Article::getCreatedAt, startTime));

            Long commentCount = commentMapper.selectCount(
                    new LambdaQueryWrapper<Comment>()
                            .eq(Comment::getUserId, user.getId())
                            .ge(Comment::getCreatedAt, startTime));

            Long activityScore = articleCount * 5 + commentCount;
            activeUsers.add(new ChartDataDTO.RankItem(
                    user.getId(), user.getNickname(), activityScore, i + 1, "stable"));
        }

        activeUsers.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        for (int i = 0; i < activeUsers.size(); i++) {
            activeUsers.get(i).setRank(i + 1);
        }
        return activeUsers;
    }

    @Override
    public ChartDataDTO.TrendData getUserGrowthComparison(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart, currentEnd, previousStart, previousEnd;

        switch (period.toLowerCase()) {
            case "week":
                currentStart = now.minusDays(7);
                currentEnd = now;
                previousStart = now.minusDays(14);
                previousEnd = now.minusDays(7);
                break;
            case "month":
                currentStart = now.minusDays(30);
                currentEnd = now;
                previousStart = now.minusDays(60);
                previousEnd = now.minusDays(30);
                break;
            default:
                currentStart = now.minusDays(1);
                currentEnd = now;
                previousStart = now.minusDays(2);
                previousEnd = now.minusDays(1);
                break;
        }

        Long currentCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().between(User::getCreatedAt, currentStart, currentEnd));
        Long previousCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().between(User::getCreatedAt, previousStart, previousEnd));

        return calculateTrendData(currentCount, previousCount, period);
    }

    @Override
    public ChartDataDTO.TrendData getArticlePublishComparison(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentStart, currentEnd, previousStart, previousEnd;

        switch (period.toLowerCase()) {
            case "week":
                currentStart = now.minusDays(7);
                currentEnd = now;
                previousStart = now.minusDays(14);
                previousEnd = now.minusDays(7);
                break;
            case "month":
                currentStart = now.minusDays(30);
                currentEnd = now;
                previousStart = now.minusDays(60);
                previousEnd = now.minusDays(30);
                break;
            default:
                currentStart = now.minusDays(1);
                currentEnd = now;
                previousStart = now.minusDays(2);
                previousEnd = now.minusDays(1);
                break;
        }

        Long currentCount = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().between(Article::getCreatedAt, currentStart, currentEnd));
        Long previousCount = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().between(Article::getCreatedAt, previousStart, previousEnd));

        return calculateTrendData(currentCount, previousCount, period);
    }

    @Override
    public List<ChartDataDTO.LineItem> getCommentActivityTrend(int days) {
        List<ChartDataDTO.LineItem> trend = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            Long count = commentMapper.selectCount(
                    new LambdaQueryWrapper<Comment>().between(Comment::getCreatedAt, dayStart, dayEnd));

            trend.add(new ChartDataDTO.LineItem(
                    date.format(DateTimeFormatter.ofPattern("MM-dd")), count, "评论数"));
        }
        return trend;
    }

    @Override
    public List<ChartDataDTO.LineItem> getViewTrend(int days) {
        List<ChartDataDTO.LineItem> trend = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Long count = (long) (Math.random() * 1000 + 500); // 示例数据
            trend.add(new ChartDataDTO.LineItem(
                    date.format(DateTimeFormatter.ofPattern("MM-dd")), count, "浏览量"));
        }
        return trend;
    }

    private ChartDataDTO.TrendData calculateTrendData(Long current, Long previous, String period) {
        Double changeRate = 0.0;
        String changeType = "stable";

        if (previous > 0) {
            changeRate = ((double) (current - previous) / previous) * 100;
            if (changeRate > 0) {
                changeType = "increase";
            } else if (changeRate < 0) {
                changeType = "decrease";
            }
        } else if (current > 0) {
            changeRate = 100.0;
            changeType = "increase";
        }

        return new ChartDataDTO.TrendData(current, previous, changeRate, changeType, period);
    }

}