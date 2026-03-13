//package com.fzg.controller.admin;
//
//import cn.dev33.satoken.annotation.SaCheckLogin;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.fzg.mapper.*;
//import com.fzg.model.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 管理员-数据统计
// */
//@RestController
//@RequestMapping("/admin/statistics")
//@SaCheckLogin
//public class AdminStatisticsController {
//
//    @Autowired
//    private UserMapper userMapper;
//    @Autowired
//    private Articlemapper articleMapper;
//    @Autowired
//    private Commentmapper commentMapper;
//    @Autowired
//    private ReportMapper reportMapper;
//
//    /**
//     * 获取系统概览数据
//     */
//    @GetMapping("/overview")
//    public Result getOverview() {
//        Map<String, Object> data = new HashMap<>();
//
//        // 用户统计
//        data.put("totalUsers", userMapper.selectCount(null));
//        data.put("activeUsers", userMapper.selectCount(
//            new LambdaQueryWrapper<User>().eq(User::getStatus, 1)));
//
//        // 文章统计
//        data.put("totalArticles", articleMapper.selectCount(null));
//        data.put("publishedArticles", articleMapper.selectCount(
//            new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1)));
//
//        // 评论统计
//        data.put("totalComments", commentMapper.selectCount(null));
//        data.put("pendingComments", commentMapper.selectCount(
//            new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 0)));
//
//        // 举报统计
//        data.put("totalReports", reportMapper.selectCount(null));
//        data.put("pendingReports", reportMapper.selectCount(
//            new LambdaQueryWrapper<Report>().eq(Report::getStatus, 0)));
//
//        return Result.success(data);
//    }
//
//    /**
//     * 获取今日数据
//     */
//    @GetMapping("/today")
//    public Result getTodayData() {
//        Map<String, Object> data = new HashMap<>();
//        // 这里需要根据实际情况添加今日数据统计逻辑
//        data.put("todayUsers", 0);
//        data.put("todayArticles", 0);
//        data.put("todayComments", 0);
//        return Result.success(data);
//    }
//
//    /**
//     * 获取趋势数据
//     */
//    @GetMapping("/trend")
//    public Result getTrendData(@RequestParam(defaultValue = "7") Integer days) {
//        // 这里需要根据实际情况添加趋势数据统计逻辑
//        return Result.success("趋势数据");
//    }
//}
