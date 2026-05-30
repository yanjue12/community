package com.fzg.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fzg.mapper.PermissionMapper;
import com.fzg.mapper.RolePermissionMapper;
import com.fzg.mapper.Rolemapper;
import com.fzg.mapper.UserPrivacyMapper;
import com.fzg.mapper.UserMapper;
import com.fzg.mapper.UserRolemapper;
import com.fzg.model.*;
import com.fzg.util.UserUtil;
import com.fzg.vo.UserAdminVO;
import com.fzg.vo.UserEditRequest;
import com.fzg.vo.UserQueryRequest;
import com.fzg.vo.UserStatsVO;
import com.fzg.vo.RoleVO;
import io.netty.util.internal.ThreadLocalRandom;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
@SaCheckRole(value = {"admin", "auditAdmin", "reportAdmin"}, mode = SaMode.OR)
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
    @Autowired
    private UserPrivacyMapper userPrivacyMapper;

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
    @SaCheckRole("admin")
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
     * 检查邮箱是否已存在
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱是否已存在")
    public Result checkEmail(@RequestParam String email) {
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getEmail, email)
        );
        return Result.success(count > 0);
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
    @PostMapping("/create")
    @SaCheckRole("admin")
    @Transactional(rollbackFor = Exception.class)
    public Result createUser(@RequestBody Map<String, Object> params) {
        User user = new User();
        user.setUsername((String) params.get("username"));
        String encryptPwd = UserUtil.getUserEncryptPassword((String) params.get("email"), (String) params.get("password"));
        user.setPassword(encryptPwd);
        user.setNickname((String) params.getOrDefault("nickname",generateRandomChineseNickname(9)));
        user.setEmail((String) params.get("email"));
        user.setPhone((String) params.get("phone"));
        user.setAvatar((String) params.getOrDefault("avatar","http://127.0.0.1:9000/website/908470.jpg"));
        user.setStatus((String) params.getOrDefault("status", "1"));
        user.setCreatedAt(Date.from(ZonedDateTime.now(ZoneId.systemDefault()).toInstant()));
        
        int result = userMapper.insert(user);
        if (result > 0) {
            initUserPrivacyIfAbsent(user.getId());

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

    private void initUserPrivacyIfAbsent(Long userId) {
        if (userId == null) {
            return;
        }
        Long count = userPrivacyMapper.selectCount(
                new LambdaQueryWrapper<UserPrivacy>().eq(UserPrivacy::getUserId, userId)
        );
        if (count != null && count > 0) {
            return;
        }

        Date now = new Date();
        UserPrivacy userPrivacy = new UserPrivacy();
        userPrivacy.setUserId(userId);
        fillDefaultPrivacySetting(userPrivacy);
        userPrivacy.setCreatedAt(now);
        userPrivacy.setUpdatedAt(now);
        userPrivacyMapper.insert(userPrivacy);
    }

    private void fillDefaultPrivacySetting(UserPrivacy userPrivacy) {
        userPrivacy.setEmailVisibility("0");
        userPrivacy.setPhoneVisibility("0");
        userPrivacy.setProfileVisibility("0");
        userPrivacy.setCanComment("0");
        userPrivacy.setArticleVisibility("0");
        userPrivacy.setLikesHidden("0");
        userPrivacy.setFavoritesHidden("0");
        userPrivacy.setFollowListHidden("0");
        userPrivacy.setFollowersListHidden("0");
        userPrivacy.setAllowPrivateMessage("0");
        userPrivacy.setAllowMention("0");
        userPrivacy.setNewFollowerNotification("0");
        userPrivacy.setAllowRecommendation("0");
        userPrivacy.setInterestBasedRecommendation("0");
        userPrivacy.setDataAnalysis("0");
        userPrivacy.setThirdPartyDataSharing("0");
    }


    // 可自行扩展字符池：常用汉字、成语字、姓氏表等
    private static final String[] HANZI_POOL = (
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ雨晨风雅星辰海棠松柏梅兰竹菊清欢陌上归舟烟波渔歌寒山流云暖阳晓月乐声逸客晴川秋水落霞孤鹜春光夏息秋实冬藏诗意浅忆墨染倾城素年"
    ).split("");

    /**
     * 生成指定长度的随机汉字昵称（允许字符重复）
     * @param length 昵称长度，建议 9
     * @return 随机汉字昵称
     */
    public static String generateRandomChineseNickname(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must > 0");
        }
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            int idx = rnd.nextInt(HANZI_POOL.length);
            sb.append(HANZI_POOL[idx]);
        }
        return sb.toString();
    }

    /**
     * 更新用户信息（包含角色授权）
     */
    @PutMapping("/{id}")
    @SaCheckRole("admin")
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



    // ==================== 角色管理 ====================



    /**
     * 查询所有角色（用户管理页面初始化下拉用）
     */
    @GetMapping("/roles")
    @Operation(summary = "获取所有角色列表")
    public Result getAllRolesForSelect() {
        return Result.success(roleMapper.selectList(
                new LambdaQueryWrapper<Role>().eq(Role::getStatus, "1").orderByAsc(Role::getSort)
        ));
    }


    /**
     * 查询所有角色（角色管理界面含权限列表和用户数量）
     */
    @GetMapping("/role/list")
    @Operation(summary = "查询所有角色", description = "返回所有角色及其权限列表、用户数量")
    public Result getAllRoles() {
        try {
            // 1. 查所有角色 + 用户数量（一条 SQL）
            List<RoleVO> roles = roleMapper.selectAllRolesWithUserCount();
            log.info("获取角色和用户数量");
            // 2. 批量查所有角色的权限（避免 N+1）
            if (!roles.isEmpty()) {
                // 查出全部 role_permission 关联
                log.info("开始查角色权限中间表");
                List<RolePermission> allRolePerms = rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<RolePermission>()
                );
                log.info("allRolePerms.size:{}",allRolePerms.size());
                // 查出全部权限
                List<Permission> allPerms = permissionMapper.selectList(new LambdaQueryWrapper<Permission>());
                log.info("permissionList.size:{}",allPerms.size());
                Map<Long, Permission> permMap = allPerms.stream()
                        .collect(Collectors.toMap(Permission::getId, p -> p));

                // 按 roleId 分组，组装到每个 RoleVO
                Map<Long, List<Long>> rolePermIds = allRolePerms.stream()
                        .collect(Collectors.groupingBy(
                                RolePermission::getRoleId,
                                Collectors.mapping(RolePermission::getPermissionId, Collectors.toList())
                        ));

                roles.forEach(role -> {
                    List<Long> permIds = rolePermIds.getOrDefault(role.getId(), java.util.Collections.emptyList());
                    List<Permission> perms = permIds.stream()
                            .map(permMap::get)
                            .filter(p -> p != null)
                            .collect(Collectors.toList());
                    role.setPermissions(perms);
                });
            }

            return Result.success(roles);
        } catch (Exception e) {
            log.error("查询角色列表失败: {}", e.getMessage(), e);
            return Result.fail(500, "查询角色列表失败");
        }
    }

    /**
     * 创建角色（可同时分配权限）
     */
    @PostMapping("/role")
    @SaCheckRole("admin")
    @Transactional(rollbackFor = Exception.class)
    public Result createRole(@RequestBody Map<String, Object> params) {
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
    @SaCheckRole("admin")
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
    @SaCheckRole("admin")
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
    @SaCheckRole("admin")
    public Result createPermission(@RequestBody Permission permission) {
        permission.setCreatedAt(new Date());
        int result = permissionMapper.insert(permission);
        return Result.handle(result > 0);
    }

    /**
     * 更新权限
     */
    @PutMapping("/permission/{id}")
    @SaCheckRole("admin")
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
    @SaCheckRole("admin")
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
