package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.model.Result;
import com.fzg.model.SystemConfig;
import com.fzg.model.SystemConfigGroup;
import com.fzg.service.SystemConfigGroupService;
import com.fzg.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 管理员-系统配置管理
 */
@RestController
@RequestMapping("/admin/system/config")
@SaCheckLogin
@SaCheckRole(value = {"admin", "auditAdmin", "reportAdmin"}, mode = SaMode.OR)
public class AdminSystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private SystemConfigGroupService systemConfigGroupService;

    // ==================== 配置管理 ====================

    /**
     * 获取全量配置列表（可按分组过滤）
     */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String groupName) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<SystemConfig>()
                .eq(groupName != null && !groupName.isEmpty(), SystemConfig::getGroupName, groupName)
                .orderByAsc(SystemConfig::getSort);
        return Result.success(systemConfigService.list(wrapper));
    }

    /**
     * 获取单条配置
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        SystemConfig config = systemConfigService.getById(id);
        return config != null ? Result.success(config) : Result.fail(404, "配置不存在");
    }

    /**
     * 新增配置
     */
    @PostMapping
    @SaCheckRole("admin")
    public Result create(@RequestBody SystemConfig config) {
        // 检查 configKey 唯一性
        Long count = systemConfigService.count(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, config.getConfigKey())
        );
        if (count > 0) {
            return Result.fail(400, "配置键已存在");
        }
        config.setCreatedAt(new Date());
        config.setUpdatedAt(new Date());
        return Result.handle(systemConfigService.save(config));
    }

    /**
     * 修改配置
     */
    @PutMapping("/{id}")
    @SaCheckRole("admin")
    public Result update(@PathVariable Long id, @RequestBody SystemConfig config) {
        config.setId(id);
        config.setUpdatedAt(new Date());
        return Result.handle(systemConfigService.updateById(config));
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    public Result delete(@PathVariable Long id) {
        return Result.handle(systemConfigService.removeById(id));
    }

    // ==================== 分组管理 ====================

    /**
     * 获取全量配置分组列表
     */
    @GetMapping("/groups")
    public Result groups() {
        return Result.success(systemConfigGroupService.list(
                new LambdaQueryWrapper<SystemConfigGroup>().orderByAsc(SystemConfigGroup::getSort)
        ));
    }

    /**
     * 新增分组
     */
    @PostMapping("/group")
    @SaCheckRole("admin")
    public Result createGroup(@RequestBody SystemConfigGroup group) {
        Long count = systemConfigGroupService.count(
                new LambdaQueryWrapper<SystemConfigGroup>().eq(SystemConfigGroup::getGroupName, group.getGroupName())
        );
        if (count > 0) {
            return Result.fail(400, "分组标识已存在");
        }
        return Result.handle(systemConfigGroupService.save(group));
    }

    /**
     * 修改分组
     */
    @PutMapping("/group/{id}")
    @SaCheckRole("admin")
    public Result updateGroup(@PathVariable Long id, @RequestBody SystemConfigGroup group) {
        group.setId(id);
        return Result.handle(systemConfigGroupService.updateById(group));
    }

    /**
     * 删除分组
     */
    @DeleteMapping("/group/{id}")
    @SaCheckRole("admin")
    public Result deleteGroup(@PathVariable Long id) {
        // 检查分组下是否有配置
        SystemConfigGroup g = systemConfigGroupService.getById(id);
        if (g != null) {
            Long count = systemConfigService.count(
                    new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getGroupName, g.getGroupName())
            );
            if (count > 0) {
                return Result.fail(400, "该分组下存在配置项，无法删除");
            }
        }
        return Result.handle(systemConfigGroupService.removeById(id));
    }
}
