package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.UserProfile;

public interface UserProfileCalculateService {

    /**
     * 计算单个用户画像
     */
    void calculateByUserId(Long userId);

    /**
     * 批量计算（定时任务用）
     */
    void calculateAll();
}

