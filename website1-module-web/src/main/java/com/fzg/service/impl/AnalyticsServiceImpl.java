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
import java.util.stream.Collectors;

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
    private final DraftMapper draftMapper;
    
    @Override
    public DashboardDTO getDashboardData() {

        articleMapper.selectCount(new LambdaQueryWrapper<>());

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setOverview(getOverviewData());
        dashboard.setUserGrowthTrend(getUserGrowthTrend("thisMonth", null, null));
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

        // 总量数据统计（原本月数据字段现在显示总量）
        overview.setCurrentMonthUsers(userMapper.selectCount(null));
        overview.setCurrentMonthArticles(articleMapper.selectCount(null));
        overview.setCurrentMonthComments(commentMapper.selectCount(null));
        
        // 本月数据统计（用于计算增长率）
        Long thisMonthUsers = userMapper.selectCount(
            new LambdaQueryWrapper<User>().between(User::getCreatedAt, 
                currentMonthStart.atStartOfDay(), currentMonthEnd.atStartOfDay()));
        
        Long thisMonthArticles = articleMapper.selectCount(
            new LambdaQueryWrapper<Article>().between(Article::getCreatedAt,
                currentMonthStart.atStartOfDay(), currentMonthEnd.atStartOfDay()));
        
        Long thisMonthComments = commentMapper.selectCount(
            new LambdaQueryWrapper<Comment>().between(Comment::getCreatedAt, 
                currentMonthStart.atStartOfDay(), currentMonthEnd.atStartOfDay()));

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
        
        // 计算增长率（本月与上月对比）
        overview.setUserGrowthRate(calculateGrowthRate(thisMonthUsers, overview.getLastMonthUsers()));
        overview.setArticleGrowthRate(calculateGrowthRate(thisMonthArticles, overview.getLastMonthArticles()));
        overview.setCommentGrowthRate(calculateGrowthRate(thisMonthComments, overview.getLastMonthComments()));
        
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
        
        // 获取今日24小时用户活跃度数据
        overview.setTodayLoginActivity(getTodayLoginActivity());
        
        // 实时数据
        overview.setOnlineUsers((long) WebSocketManager.getOnlineUserCount());
        
        // 浏览量数据 (如果有浏览记录表的话)
        // 总浏览量（原本月浏览量字段现在显示总量）
        overview.setCurrentMonthViews(0L);  // 这里应该是总浏览量，如果有浏览记录表可以查询总数
        overview.setLastMonthViews(0L);     // 上月浏览量
        overview.setTotalViews(0L);         // 总浏览量
        overview.setTodayViews(0L);         // 今日浏览量
        overview.setViewGrowthRate(BigDecimal.ZERO);  // 本月与上月浏览量增长率
        
        return overview;
    }

    /**
     * 获取今日24小时用户登录活跃度数据
     * 基于User表的lastLoginTime字段统计各时间段的登录人数
     */
    private List<DashboardDTO.HourlyLoginData> getTodayLoginActivity() {
        List<DashboardDTO.HourlyLoginData> hourlyData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        int currentHour = now.getHour();
        
        // 初始化24小时数据，默认为0
        for (int hour = 0; hour < 24; hour++) {
            String timeLabel = String.format("%02d:00", hour);
            hourlyData.add(new DashboardDTO.HourlyLoginData(hour, 0L, timeLabel));
        }
        
        try {
            // 查询今天整天的登录用户，然后在代码中过滤时间
            Date todayStart = Date.from(today.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
            Date todayEnd = Date.from(today.plusDays(1).atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
            
            // 查询今日登录的用户（lastLoginTime在今天范围内）
            List<User> todayLoginUsers = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .between(User::getLastLoginTime, todayStart, todayEnd)
                    .isNotNull(User::getLastLoginTime)
            );
            log.info("查询到今日登录用户数量: {}", todayLoginUsers.size());
            
            // 按小时分组统计登录人数，并过滤只统计已经过去的时间
            Map<Integer, Long> hourlyStats = todayLoginUsers.stream()
                .filter(user -> user.getLastLoginTime() != null)
                .map(user -> {
                    // 将Date转换为LocalDateTime获取小时
                    LocalDateTime loginTime = user.getLastLoginTime().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
                    return loginTime.getHour();
                })
                .filter(hour -> hour <= currentHour) // 只统计已经过去的小时
                .collect(Collectors.groupingBy(
                    hour -> hour,
                    Collectors.counting()
                ));
            
            log.info("按小时统计结果: {}", hourlyStats);
            
            // 更新对应小时的数据
            for (Map.Entry<Integer, Long> entry : hourlyStats.entrySet()) {
                Integer hour = entry.getKey();
                Long count = entry.getValue();
                if (hour >= 0 && hour < 24) {
                    hourlyData.get(hour).setLoginCount(count);
                    log.info("更新{}点数据: {}", hour, count);
                }
            }
            
        } catch (Exception e) {
            log.warn("获取今日用户登录活跃度数据失败: {}", e.getMessage());
            // 如果查询失败，返回当前时间之前的模拟数据
            return generateMockHourlyDataUntilNow();
        }
        
        return hourlyData;
    }
    
    /**
     * 生成模拟的24小时活跃度数据（只到当前时间）
     */
    private List<DashboardDTO.HourlyLoginData> generateMockHourlyDataUntilNow() {
        List<DashboardDTO.HourlyLoginData> mockData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        
        // 模拟一天的用户活跃度曲线（早上和晚上活跃度较高）
        int[] mockCounts = {
            2, 1, 0, 0, 1, 3, 8, 15, 25, 30, 28, 32,  // 0-11点
            35, 28, 25, 22, 20, 25, 30, 35, 28, 15, 8, 5   // 12-23点
        };
        
        for (int hour = 0; hour < 24; hour++) {
            String timeLabel = String.format("%02d:00", hour);
            Long count = 0L;
            
            // 只有已经过去的时间才有数据
            if (hour <= currentHour) {
                count = (long) mockCounts[hour];
            }
            
            mockData.add(new DashboardDTO.HourlyLoginData(hour, count, timeLabel));
        }
        
        return mockData;
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
    public List<ChartDataDTO.LineItem> getUserGrowthTrend(String timeType, LocalDate startDate, LocalDate endDate) {
        List<ChartDataDTO.LineItem> trend = new ArrayList<>();
        
        switch (timeType.toLowerCase()) {
            case "today":
                return getUserGrowthTrendByHour(LocalDate.now());
            case "thismonth":
                return getUserGrowthTrendByDay(LocalDate.now().withDayOfMonth(1), LocalDate.now());
            case "thisyear":
                return getUserGrowthTrendByMonth(LocalDate.now().withDayOfYear(1), LocalDate.now());
            case "custom":
                return getUserGrowthTrendCustom(startDate, endDate);
            default:
                throw new IllegalArgumentException("不支持的时间类型: " + timeType);
        }
    }
    
    /**
     * 按小时统计用户增长趋势（当天）
     */
    private List<ChartDataDTO.LineItem> getUserGrowthTrendByHour(LocalDate date) {
        List<ChartDataDTO.LineItem> trend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        
        for (int hour = 0; hour < 24; hour++) {
            LocalDateTime hourStart = date.atTime(hour, 0);
            LocalDateTime hourEnd = hourStart.plusHours(1);
            
            Long count = 0L;
            // 只统计已经过去的小时
            if (date.isBefore(LocalDate.now()) || hour <= currentHour) {
                count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().between(User::getCreatedAt, hourStart, hourEnd));
            }
            
            String timeLabel = String.format("%02d:00", hour);
            trend.add(new ChartDataDTO.LineItem(timeLabel, count, 0L, "新增用户"));
        }
        return trend;
    }
    
    /**
     * 按天统计用户增长趋势
     */
    private List<ChartDataDTO.LineItem> getUserGrowthTrendByDay(LocalDate startDate, LocalDate endDate) {
        List<ChartDataDTO.LineItem> trend = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);

            Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().between(User::getCreatedAt, dayStart, dayEnd));

            trend.add(new ChartDataDTO.LineItem(
                date.format(DateTimeFormatter.ofPattern("MM-dd")), count, 0L, "新增用户"));
        }
        return trend;
    }
    
    /**
     * 按月统计用户增长趋势
     */
    private List<ChartDataDTO.LineItem> getUserGrowthTrendByMonth(LocalDate startDate, LocalDate endDate) {
        List<ChartDataDTO.LineItem> trend = new ArrayList<>();
        
        LocalDate monthStart = startDate.withDayOfMonth(1);
        LocalDate monthEnd = endDate.withDayOfMonth(1);
        
        for (LocalDate month = monthStart; !month.isAfter(monthEnd); month = month.plusMonths(1)) {
            LocalDateTime monthStartTime = month.atStartOfDay();
            LocalDateTime monthEndTime = month.plusMonths(1).atStartOfDay();

            Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().between(User::getCreatedAt, monthStartTime, monthEndTime));

            trend.add(new ChartDataDTO.LineItem(
                month.format(DateTimeFormatter.ofPattern("yyyy-MM")), count, 0L, "新增用户"));
        }
        return trend;
    }
    
    /**
     * 自定义时间段统计用户增长趋势
     */
    private List<ChartDataDTO.LineItem> getUserGrowthTrendCustom(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("自定义时间段的开始和结束日期不能为空");
        }
        
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        
        // 判断时间维度
        if (daysBetween == 0) {
            // 同一天，按小时统计
            return getUserGrowthTrendByHour(startDate);
        } else if (daysBetween > 365) {
            // 大于一年，按月统计
            return getUserGrowthTrendByMonth(startDate, endDate);
        } else if (daysBetween > 31) {
            // 大于一个月，按月统计
            return getUserGrowthTrendByMonth(startDate, endDate);
        } else {
            // 小于等于一个月，按天统计
            return getUserGrowthTrendByDay(startDate, endDate);
        }
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

            Long draftCount = draftMapper.selectCount(new LambdaQueryWrapper<Draft>().between(Draft::getLastModifiedAt, dayStart, dayEnd));
            trend.add(new ChartDataDTO.LineItem(
                    date.format(DateTimeFormatter.ofPattern("MM-dd")), count,draftCount, "发布文章"));
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
                    date.format(DateTimeFormatter.ofPattern("MM-dd")), count,0L, "评论数"));
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
                    date.format(DateTimeFormatter.ofPattern("MM-dd")), count,0L, "浏览量"));
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