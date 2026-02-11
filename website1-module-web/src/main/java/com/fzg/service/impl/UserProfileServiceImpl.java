package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fzg.mapper.UserBehaviorLogMapper;
import com.fzg.mapper.UserProfileMapper;
import com.fzg.model.UserBehaviorLog;
import com.fzg.model.UserProfile;
import com.fzg.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rebuildUserProfile(Long userId) {

        // 1️⃣ 查最近30天行为
        List<UserBehaviorLog> logs =
                behaviorLogMapper.selectRecentBehavior(userId);

        if (CollectionUtils.isEmpty(logs)) {
            return;
        }

        Map<Long, Double> tagScore = new HashMap<>();
        Map<Long, Double> categoryScore = new HashMap<>();
        Map<Long, Double> authorScore = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();

        for (UserBehaviorLog log : logs) {

            double decay = calculateDecay(log.getCreateAt(), now);
            double finalWeight = log.getBehaviorWeight() * decay;

            if (log.getTagId() != null) {
                tagScore.merge(log.getTagId(), finalWeight, Double::sum);
            }

            if (log.getCategoryId() != null) {
                categoryScore.merge(log.getCategoryId(), finalWeight, Double::sum);
            }

            if (log.getAuthorId() != null) {
                authorScore.merge(log.getAuthorId(), finalWeight, Double::sum);
            }
        }

        // 2️⃣ 更新画像表
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setTagProfile(tagScore);
        profile.setCategoryProfile(categoryScore);
        profile.setAuthorProfile(authorScore);
        profile.setBehaviorCount(logs.size());
        profile.setProfileLevel(calculateLevel(logs.size()));
        profile.setLastCalculatedAt(LocalDateTime.now());

        userProfileMapper.upsert(profile);
    }


    private double calculateDecay(LocalDateTime behaviorTime, LocalDateTime now) {

        long days = ChronoUnit.DAYS.between(behaviorTime, now);

        if (days <= 7) {
            return 1.0;
        } else if (days <= 30) {
            return 0.5;
        } else {
            return 0;
        }
    }

    private int calculateLevel(int count) {

        if (count < 10) return 0;
        if (count < 30) return 1;
        if (count < 80) return 2;
        return 3;
    }


}
