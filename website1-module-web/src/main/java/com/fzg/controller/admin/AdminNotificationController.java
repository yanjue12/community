package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.fzg.enums.EnumReturn;
import com.fzg.model.Result;
import com.fzg.task.NotificationPartitionTask;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员通知管理接口
 */
@RestController
@RequestMapping("/api/admin/notification")
@RequiredArgsConstructor
@Slf4j
@SaCheckLogin
@SaCheckRole(value = {"admin", "auditAdmin", "reportAdmin"}, mode = SaMode.OR)
@Tag(name = "管理员通知管理", description = "通知分区管理等管理员功能")
public class AdminNotificationController {

    private final NotificationPartitionTask partitionTask;

    /**
     * 手动创建指定月份的分区
     */
    @PostMapping("/partition/create")
    @SaCheckRole("admin")
    @Operation(summary = "创建指定月份分区")
    public Result<String> createPartition(@RequestParam int year, @RequestParam int month) {
        try {
            if (year < 2024 || year > 2030) {
                return Result.fail(EnumReturn.valueOf("年份必须在2024-2030之间"));
            }
            if (month < 1 || month > 12) {
                return Result.fail(EnumReturn.valueOf("月份必须在1-12之间"));
            }
            
            partitionTask.createPartitionForMonth(year, month);
            return Result.success(String.format("成功创建%d年%d月分区", year, month));
        } catch (Exception e) {
            log.error("创建分区失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("创建分区失败: " + e.getMessage()));
        }
    }

    /**
     * 手动删除指定分区
     */
    @DeleteMapping("/partition/{partitionName}")
    @SaCheckRole("admin")
    @Operation(summary = "删除指定分区")
    public Result<String> dropPartition(@PathVariable String partitionName) {
        try {
            // 安全检查，防止删除重要分区
            if ("p_future".equals(partitionName)) {
                return Result.fail(EnumReturn.valueOf("不能删除未来分区"));
            }
            
            partitionTask.dropPartition(partitionName);
            return Result.success("成功删除分区: " + partitionName);
        } catch (Exception e) {
            log.error("删除分区失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("删除分区失败: " + e.getMessage()));
        }
    }

    /**
     * 手动执行下个月分区创建
     */
    @PostMapping("/partition/create-next-month")
    @SaCheckRole("admin")
    @Operation(summary = "创建下个月分区")
    public Result<String> createNextMonthPartition() {
        try {
            partitionTask.createNextMonthPartition();
            return Result.success("成功创建下个月分区");
        } catch (Exception e) {
            log.error("创建下个月分区失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("创建下个月分区失败: " + e.getMessage()));
        }
    }

    /**
     * 手动执行历史分区删除
     */
    @PostMapping("/partition/drop-old")
    @SaCheckRole("admin")
    @Operation(summary = "删除历史分区")
    public Result<String> dropOldPartitions() {
        try {
            partitionTask.dropOldPartitions();
            return Result.success("成功删除历史分区");
        } catch (Exception e) {
            log.error("删除历史分区失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("删除历史分区失败: " + e.getMessage()));
        }
    }

    /**
     * 查看分区状态
     */
    @GetMapping("/partition/status")
    @Operation(summary = "查看分区状态")
    public Result<Map<String, Object>> getPartitionStatus() {
        try {
            // 获取分区信息
            List<Map<String, Object>> partitions = partitionTask.getPartitionInfo();
            
            // 统计信息
            long totalRows = 0;
            long totalDataLength = 0;
            int activePartitions = 0;
            
            for (Map<String, Object> partition : partitions) {
                Long tableRows = (Long) partition.get("TABLE_ROWS");
                Long dataLength = (Long) partition.get("DATA_LENGTH");
                
                tableRows = tableRows != null ? tableRows : 0L;
                dataLength = dataLength != null ? dataLength : 0L;
                
                totalRows += tableRows;
                totalDataLength += dataLength;
                activePartitions++;
            }
            
            // 构建响应数据
            Map<String, Object> result = new HashMap<>();
            result.put("partitions", partitions);
            result.put("summary", Map.of(
                "totalPartitions", activePartitions,
                "totalRows", totalRows,
                "totalDataSize", formatBytes(totalDataLength),
                "totalDataSizeBytes", totalDataLength
            ));
            
            // 同时输出到日志
            partitionTask.monitorPartitionStatus();
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("查看分区状态失败: {}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("查看分区状态失败: " + e.getMessage()));
        }
    }
    
    /**
     * 格式化字节数为可读格式
     */
    private String formatBytes(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f%s", size, units[unitIndex]);
    }
}