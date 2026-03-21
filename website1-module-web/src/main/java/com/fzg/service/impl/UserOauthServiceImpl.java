package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.UserOauthMapper;
import com.fzg.model.UserOauth;
import com.fzg.service.UserOauthService;
import org.springframework.stereotype.Service;

@Service
public class UserOauthServiceImpl extends ServiceImpl<UserOauthMapper, UserOauth> implements UserOauthService {

    @Override
    public UserOauth getByOauthTypeAndOpenId(String oauthType, String openId) {
        return baseMapper.selectOne(
                new LambdaQueryWrapper<UserOauth>()
                        .eq(UserOauth::getOauthType, oauthType)
                        .eq(UserOauth::getOpenId, openId)
        );
    }
}
