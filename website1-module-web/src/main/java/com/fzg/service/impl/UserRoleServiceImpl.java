package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.model.UserRole;
import com.fzg.service.UserRoleService;
import com.fzg.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

/**
* @author yanju
* @description 针对表【user_role(用户角色关联表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:29
*/
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole>
    implements UserRoleService{

}




