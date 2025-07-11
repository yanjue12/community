package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Role;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yanju
* @description 针对表【role(角色表)】的数据库操作Mapper
* @createDate 2025-07-09 17:09:29
* @Entity model.Role
*/
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

}




