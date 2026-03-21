package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.UserOauth;

public interface UserOauthService extends IService<UserOauth> {

    /** 根据授权类型和 openId 查询绑定记录 */
    UserOauth getByOauthTypeAndOpenId(String oauthType, String openId);
}
