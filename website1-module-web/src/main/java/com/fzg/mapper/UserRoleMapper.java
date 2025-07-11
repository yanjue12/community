package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yanju
* @description 针对表【user_role(用户角色关联表)】的数据库操作Mapper
* @createDate 2025-07-09 17:09:29
* @Entity model.UserRole
*/
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

}




