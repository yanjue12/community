package com.fzg.service;

import com.fzg.enums.BehaviorTypeEnum;

public interface UserBehaviorService {

    void recordBehavior(
        Long userId,
        Long articleId,
        BehaviorTypeEnum behaviorType
    );
}
