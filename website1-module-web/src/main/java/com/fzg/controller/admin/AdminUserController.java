package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.mapper.PermissionMapper;
import com.fzg.mapper.RolePermissionMapper;
import com.fzg.mapper.Rolemapper;
import com.fzg.mapper.UserMapper;
import com.fzg.mapper.UserRolemapper;
import com.fzg.model.*;
import com.fzg.vo.UserAdminVO;
import com.fzg.vo.UserEditRequest;
import com.fzg.vo.UserQueryRequest;
import com.fzg.vo.UserStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员-用户、角色、权限管理
 */
@RestController
@RequestMapping("/admin/user")
@SaCheckLogin
@Slf4j
@Tag(name = "管理端用户管理", description = "用户管理相关接口")
public class AdminUserController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private Rolemapper roleMapper;
    @Autowired
    private PermissionMapper permissionMapper;
    @Autowired
    private UserRolemapper userRoleMapper;
    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    // ==================== 用户管理 ====================

    /**
     * 用户管理卡片统计（总数、正常、封禁、管理员）
     */
    @GetMapping("/statistics")
    @Operation(summary = "用户卡片统计")
    public Result getUserStats() {
        try {
            UserStatsVO stats = userMapper.queryUserStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("查询用户统计失败: {}", e.getMessage(), e);
            return Result.fail(500, "查询失败");
        }
    }

    /**
     * 用户列表（用户名/角色/状态多条件分页查询）
     */
    @PostMapping("/list")
    @Operation(summary = "用户列表查询")
    public Result listUsers(@RequestBody(required = false) UserQueryRequest req) {
        try {
            if (req == null) req = new UserQueryRequest();
            int pageNum  = req.getPageNum()  == null ? 1  : req.getPageNum();
            int pageSize = req.getPageSize() == null ? 10 : req.getPageSize();
            req.setPageNum(pageNum);
            req.setPageSize(pageSize);
            int offset = (pageNum - 1) * pageSize;

            List<UserAdminVO> list = userMapper.queryAdminUserList(req, offset);
            Long total = userMapper.countAdminUserList(req);

            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total);
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询用户列表失败: {}", e.getMessage(), e);
            return Result.fail(500, "查询失败");
        }
    }

    /**
     * 编辑用户（状态 + 角色）
     */
    @PutMapping("/edit/{id}")
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "编辑用户状态和角色")
    public Result editUser(@PathVariable Long id, @RequestBody UserEditRequest req) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        // 更新状态
        if (req.getStatus() != null) {
            user.setStatus(req.getStatus());
            user.setUpdatedAt(new Date());
            userMapper.updateById(user);
        }
        // 更新角色
        if (req.getRoleId() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
            UserRole userRole = new UserRole();
            userRole.setUserId(id);
            userRole.setRoleId(req.getRoleId());
            userRole.setCreatedAt(new Date());
            userRoleMapper.insert(userRole);
        }
        return Result.success(true);
    }

    /**
     * 查询所有角色（页面初始化下拉用）
     */
    @GetMapping("/roles")
    @Operation(summary = "获取所有角色列表")
    public Result getAllRolesForSelect() {
        return Result.success(roleMapper.selectList(
            new LambdaQueryWrapper<Role>().eq(Role::getStatus, "1").orderByAsc(Role::getSort)
        ));
    }

    /**
     * 获取用户详情（包含角色信息）
     */
    @GetMapping("/{id}")
    public Result getUser(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        
        // 查询用户的角色列表
        List<UserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id)
        );
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roleIds", roleIds);
        
        return Result.success(result);
    }

    /**
     * 创建用户（可同时分配角色）
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result createUser(@RequestBody java.util.Map<String, Object> params) {
        User user = new User();
        user.setUsername((String) params.get("username"));
        user.setPassword((String) params.get("password")); // 实际应该加密
        user.setNickname((String) params.get("nickname"));
        user.setEmail((String) params.get("email"));
        user.setPhone((String) params.get("phone"));
        user.setAvatar((String) params.get("avatar"));
        user.setStatus((String) params.getOrDefault("status", "1"));
        user.setCreatedAt(new Date());
        
        int result = userMapper.insert(user);
        if (result > 0) {
            // 分配角色
            List<Integer> roleIds = (List<Integer>) params.get("roleIds");
            if (!CollectionUtils.isEmpty(roleIds)) {
                for (Integer roleId : roleIds) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(user.getId());
                    userRole.setRoleId(roleId.longValue());
                    userRole.setCreatedAt(new Date());
                    userRoleMapper.insert(userRole);
                }
            }
        }
        return Result.handle(result > 0);
    }

    /**
     * 更新用户信息（包含角色授权）
     */
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result updateUser(@PathVariable Long id, @RequestBody java.util.Map<String, Object> params) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(404, "用户不存在");
        }
        
        // 更新用户基本信息
        if (params.containsKey("nickname")) user.setNickname((String) params.get("nickname"));
        if (params.containsKey("email")) user.setEmail((String) params.get("email"));
        if (params.containsKey("phone")) user.setPhone((String) params.get("phone"));
        if (params.containsKey("avatar")) user.setAvatar((String) params.get("avatar"));
        if (params.containsKey("status")) user.setStatus((String) params.get("status"));
        user.setUpdatedAt(new Date());
        
        int result = userMapper.updateById(user);
        
        // 更新用户角色
        if (params.containsKey("roleIds")) {
            // 删除原有角色
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
            
            // 添加新角色
            List<Integer> roleIds = (List<Integer>) params.get("roleIds");
            if (!CollectionUtils.isEmpty(roleIds)) {
                for (Integer roleId : roleIds) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(id);
                    userRole.setRoleId(roleId.longValue());
                    userRole.setCreatedAt(new Date());
                    userRoleMapper.insert(userRole);
                }
            }
        }
        
        return Result.handle(result > 0);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result deleteUser(@PathVariable Long id) {
        // 删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        // 删除用户
        int result = userMapper.deleteById(id);
        return Result.handle(result > 0);
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @Transactional(rollbackFor = Exception.class)
    public Result batchDelete(@RequestBody List<Long> ids) {
        // 删除用户角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, ids));
        // 删除用户
        int result = userMapper.deleteBatchIds(ids);
        return Result.handle(result > 0);
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-password")
    public Result resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        User user = new User();
        user.setId(id);
        user.setPassword(newPassword); // 实际应该加密
        user.setUpdatedAt(new Date());
        int result = userMapper.updateById(user);
        return Result.handle(result > 0);
    }

    // ==================== 角色管理 ====================
    
    /**
     * 查询角色列表（支持分页）
     */
    @GetMapping("/role/list")
    public Result listRoles(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String keyword) {
        Page<Role> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Role::getRoleName, keyword)
                   .or().like(Role::getRoleCode, keyword);
        }
        wrapper.orderByDesc(Role::getCreatedAt);
        return Result.success(roleMapper.selectPage(page, wrapper));
    }

    /**
     * 获取所有角色（不分页）
     */
    @GetMapping("/role/all")
    public Result getAllRoles() {
        return Result.success(roleMapper.selectList(
            new LambdaQueryWrapper<Role>().orderByAsc(Role::getCreatedAt)
        ));
    }

    /**
     * 获取角色详情（包含权限）
     */
    @GetMapping("/role/{id}")
    public Result getRole(@PathVariable Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return Result.fail(404, "角色不存在");
        }
        
        // 查询角色的权限列表
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(id);
        List<Long> permissionIds = rolePermissions.stream()
            .map(RolePermission::getPermissionId).collect(Collectors.toList());
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("role", role);
        result.put("permissionIds", permissionIds);
        
        return Result.success(result);
    }

    /**
     * 创建角色（可同时分配权限）
     */
    @PostMapping("/role")
    @Transactional(rollbackFor = Exception.class)
    public Result createRole(@RequestBody java.util.Map<String, Object> params) {
        Role role = new Role();
        role.setRoleName((String) params.get("roleName"));
        role.setRoleCode((String) params.get("roleCode"));
        role.setDescription((String) params.get("description"));
        role.setStatus((String) params.getOrDefault("status", "1"));
        role.setCreatedAt(new Date());
        
        int result = roleMapper.insert(role);
        if (result > 0) {
            // 分配权限
            List<Integer> permissionIds = (List<Integer>) params.get("permissionIds");
            if (!CollectionUtils.isEmpty(permissionIds)) {
                for (Integer permissionId : permissionIds) {
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRoleId(role.getId());
                    rolePermission.setPermissionId(permissionId.longValue());
                    rolePermission.setCreatedAt(new Date());
                    rolePermissionMapper.insert(rolePermission);
                }
            }
        }
        return Result.handle(result > 0);
    }

    /**
     * 更新角色（包含权限分配）
     */
    @PutMapping("/role/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result updateRole(@PathVariable Long id, @RequestBody java.util.Map<String, Object> params) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return Result.fail(404, "角色不存在");
        }
        
        // 更新角色基本信息
        if (params.containsKey("roleName")) role.setRoleName((String) params.get("roleName"));
        if (params.containsKey("roleCode")) role.setRoleCode((String) params.get("roleCode"));
        if (params.containsKey("description")) role.setDescription((String) params.get("description"));
        if (params.containsKey("status")) role.setStatus((String) params.get("status"));
        role.setCreatedAt(new Date());
        
        int result = roleMapper.updateById(role);
        
        // 更新角色权限
        if (params.containsKey("permissionIds")) {
            // 删除原有权限
            rolePermissionMapper.delete(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id)
            );
            
            // 添加新权限
            List<Integer> permissionIds = (List<Integer>) params.get("permissionIds");
            if (!CollectionUtils.isEmpty(permissionIds)) {
                for (Integer permissionId : permissionIds) {
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRoleId(id);
                    rolePermission.setPermissionId(permissionId.longValue());
                    rolePermission.setCreatedAt(new Date());
                    rolePermissionMapper.insert(rolePermission);
                }
            }
        }
        
        return Result.handle(result > 0);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/role/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result deleteRole(@PathVariable Long id) {
        // 检查是否有用户使用该角色
        Long count = userRoleMapper.selectCount(
            new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id)
        );
        if (count > 0) {
            return Result.fail(400, "该角色下有用户，无法删除");
        }
        
        // 删除角色权限关联
        rolePermissionMapper.delete(
            new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id)
        );
        // 删除角色
        int result = roleMapper.deleteById(id);
        return Result.handle(result > 0);
    }

    // ==================== 权限管理 ====================
    
    /**
     * 查询权限列表（支持分页）
     */
    @GetMapping("/permission/list")
    public Result listPermissions(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) String keyword) {
        Page<Permission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Permission::getName, keyword)
                   .or().like(Permission::getPermissionCode, keyword);
        }
        wrapper.orderByAsc(Permission::getSort).orderByDesc(Permission::getCreatedAt);
        return Result.success(permissionMapper.selectPage(page, wrapper));
    }

    /**
     * 获取所有权限（树形结构）
     */
    @GetMapping("/permission/tree")
    public Result getPermissionTree() {
        List<Permission> permissions = permissionMapper.selectList(
            new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getSort)
        );
        return Result.success(permissions);
    }

    /**
     * 获取权限详情
     */
    @GetMapping("/permission/{id}")
    public Result getPermission(@PathVariable Long id) {
        Permission permission = permissionMapper.selectById(id);
        return permission != null ? Result.success(permission) : Result.fail(404, "权限不存在");
    }

    /**
     * 创建权限
     */
    @PostMapping("/permission")
    public Result createPermission(@RequestBody Permission permission) {
        permission.setCreatedAt(new Date());
        int result = permissionMapper.insert(permission);
        return Result.handle(result > 0);
    }

    /**
     * 更新权限
     */
    @PutMapping("/permission/{id}")
    public Result updatePermission(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        permission.setUpdatedAt(new Date());
        int result = permissionMapper.updateById(permission);
        return Result.handle(result > 0);
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/permission/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result deletePermission(@PathVariable Long id) {
        // 检查是否有角色使用该权限
        Long count = rolePermissionMapper.selectCount(
            new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getPermissionId, id)
        );
        if (count > 0) {
            return Result.fail(400, "该权限已被角色使用，无法删除");
        }
        
        // 检查是否有子权限
        Long childCount = permissionMapper.selectCount(
            new LambdaQueryWrapper<Permission>().eq(Permission::getParentId, id)
        );
        if (childCount > 0) {
            return Result.fail(400, "该权限下有子权限，无法删除");
        }
        
        int result = permissionMapper.deleteById(id);
        return Result.handle(result > 0);
    }
}
