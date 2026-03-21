package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.Role;
import com.fzg.vo.RoleVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Rolemapper extends BaseMapper<Role> {
    int deleteByPrimaryKey(Long id);

    int insert(Role record);

    int insertSelective(Role record);

    Role selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Role record);

    int updateByPrimaryKey(Role record);

    /** 查询所有角色（含用户数量），权限列表在 Service 层组装 */
    List<RoleVO> selectAllRolesWithUserCount();
}