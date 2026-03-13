//package com.fzg.controller.admin;
//
//import cn.dev33.satoken.annotation.SaCheckLogin;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.fzg.mapper.ReportMapper;
//import com.fzg.model.Report;
//import com.fzg.model.Result;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
///**
// * 管理员-举报管理
// */
//@RestController
//@RequestMapping("/admin/report")
//@SaCheckLogin
//public class AdminReportController {
//
//    @Autowired
//    private ReportMapper reportMapper;
//
//    /**
//     * 分页查询举报列表
//     */
//    @GetMapping("/list")
//    public Result listReports(@RequestParam(defaultValue = "1") Integer pageNum,
//                             @RequestParam(defaultValue = "10") Integer pageSize,
//                             @RequestParam(required = false) Integer reportType,
//                             @RequestParam(required = false) Integer status) {
//        Page<Report> page = new Page<>(pageNum, pageSize);
//        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
//        if (reportType != null) {
//            wrapper.eq(Report::getReportType, reportType);
//        }
//        if (status != null) {
//            wrapper.eq(Report::getStatus, status);
//        }
//        wrapper.orderByDesc(Report::getCreateTime);
//        return Result.success(reportMapper.selectPage(page, wrapper));
//    }
//
//    /**
//     * 获取举报详情
//     */
//    @GetMapping("/{id}")
//    public Result getReport(@PathVariable Long id) {
//        Report report = reportMapper.selectById(id);
//        return report != null ? Result.success(report) : Result.fail(404, "举报记录不存在");
//    }
//
//    /**
//     * 处理举报
//     */
//    @PutMapping("/{id}/handle")
//    public Result handleReport(@PathVariable Long id,
//                              @RequestParam Integer status,
//                              @RequestParam Long adminId,
//                              @RequestParam(required = false) String handleResult) {
//        Report report = new Report();
//        report.setId(id);
//        report.setStatus(status);
//        report.setHandleAdminId(adminId);
//        report.setHandleResult(handleResult);
//        report.setHandleTime(new java.util.Date());
//        int result = reportMapper.updateById(report);
//        return Result.handle(result > 0);
//    }
//
//    /**
//     * 批量处理举报
//     */
//    @PutMapping("/batch/handle")
//    public Result batchHandle(@RequestBody java.util.List<Long> ids,
//                             @RequestParam Integer status,
//                             @RequestParam Long adminId) {
//        for (Long id : ids) {
//            Report report = new Report();
//            report.setId(id);
//            report.setStatus(status);
//            report.setHandleAdminId(adminId);
//            report.setHandleTime(new java.util.Date());
//            reportMapper.updateById(report);
//        }
//        return Result.success(true);
//    }
//
//    /**
//     * 删除举报记录
//     */
//    @DeleteMapping("/{id}")
//    public Result deleteReport(@PathVariable Long id) {
//        int result = reportMapper.deleteById(id);
//        return Result.handle(result > 0);
//    }
//
//    /**
//     * 获取待处理举报数量
//     */
//    @GetMapping("/pending/count")
//    public Result getPendingCount() {
//        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(Report::getStatus, 0); // 0-待处理
//        Long count = reportMapper.selectCount(wrapper);
//        return Result.success(count);
//    }
//
//    /**
//     * 按类型统计举报
//     */
//    @GetMapping("/statistics/type")
//    public Result getStatisticsByType() {
//        // 这里可以使用自定义SQL进行统计
//        return Result.success("统计数据");
//    }
//}
