package com.fzg.Interceptor;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {
    @Override
    public List<String> getPermissionList(Object o, String s) {
        return null; // 根据需要实现权限逻辑
    }

    @Override
    public List<String> getRoleList(Object userId, String s) {
        List<String> list = new ArrayList<>();
        // 根据用户 ID 返回对应的角色
        if (userId != null) {
            // 假设根据 userId 查找角色，这里只是示例
            if (userId.equals("1")) {
                list.add("admin");
            } else {
                list.add("user");
            }
        }
        return list; // 返回角色列表
    }
}
