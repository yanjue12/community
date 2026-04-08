package com.fzg.Interceptor;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.mapper.Rolemapper;
import com.fzg.mapper.UserRolemapper;
import com.fzg.model.Role;
import com.fzg.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserRolemapper userRolemapper;
    private final Rolemapper rolemapper;

    @Override
    public List<String> getPermissionList(Object o, String s) {
        return null; // 根据需要实现权限逻辑
    }

    @Override
    public List<String> getRoleList(Object userId, String s) {
        List<String> list = new ArrayList<>();
        if (userId != null) {
            Long uid = Long.valueOf(String.valueOf(userId));
            List<UserRole> userRoles = userRolemapper.selectList(
                    new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, uid)
            );
            if (!userRoles.isEmpty()) {
                List<Long> roleIds = userRoles.stream()
                        .map(UserRole::getRoleId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!roleIds.isEmpty()) {
                    List<Role> roles = rolemapper.selectBatchIds(roleIds);
                    roles.stream()
                            .map(Role::getRoleCode)
                            .filter(code -> code != null && !code.isEmpty())
                            .forEach(list::add);
                }

                userRoles.stream()
                        .map(UserRole::getRoleName)
                        .filter(name -> name != null && !name.isEmpty())
                        .forEach(list::add);
            }
        }

        if (list.isEmpty()) {
            list.add("user");
        }
        return list; // 返回角色列表
    }
}
