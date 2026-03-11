package com.fzg.service;



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

