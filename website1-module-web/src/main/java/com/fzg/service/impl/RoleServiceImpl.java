package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.model.Role;
import com.fzg.service.RoleService;
import com.fzg.mapper.RoleMapper;
import org.springframework.stereotype.Service;

/**
* @author yanju
* @description 针对表【role(角色表)】的数据库操作Service实现
* @createDate 2025-07-09 17:09:29
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService{

}




