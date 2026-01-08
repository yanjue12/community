package com.fzg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fzg.model.SignInLog;
import org.springframework.stereotype.Repository;

@Repository
public interface SignInLogmapper extends BaseMapper<SignInLog> {
    int deleteByPrimaryKey(Long id);

    int insert(SignInLog record);

    int insertSelective(SignInLog record);

    SignInLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(SignInLog record);

    int updateByPrimaryKey(SignInLog record);
}