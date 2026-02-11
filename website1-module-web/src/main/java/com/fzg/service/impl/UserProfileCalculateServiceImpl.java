package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fzg.mapper.UserBehaviorLogMapper;
import com.fzg.mapper.UserProfileMapper;
import com.fzg.model.UserBehaviorLog;
import com.fzg.model.UserProfile;
import com.fzg.service.UserProfileCalculateService;
import com.fzg.util.BehaviorDecayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserProfileCalculateServiceImpl
        implements UserProfileCalculateService {

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    private static final int PROFILE_DAYS = 90;

    @Override
    @Transactional
    public void calculateByUserId(Long userId) {

        List<UserBehaviorLog> logs =
                behaviorLogMapper.selectRecentByUser(userId, PROFILE_DAYS);

        if (CollectionUtils.isEmpty(logs)) {
            return;
        }

        Map<Long, Double> tagMap = new HashMap<>();
        Map<Long, Double> categoryMap = new HashMap<>();
        Map<Long, Double> authorMap = new HashMap<>();

        int behaviorCount = 0;

        for (UserBehaviorLog log : logs) {

            double factor = BehaviorDecayUtil.decay(log.getCreateAt());
            if (factor <= 0) continue;

            double score = log.getBehaviorWeight() * factor;
            behaviorCount++;

            if (log.getTagId() != null) {
                tagMap.merge(log.getTagId(), score, Double::sum);
            }
            if (log.getCategoryId() != null) {
                categoryMap.merge(log.getCategoryId(), score, Double::sum);
            }
            if (log.getAuthorId() != null) {
                authorMap.merge(log.getAuthorId(), score, Double::sum);
            }
        }

        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setTagProfile(topN(tagMap, 10));
        profile.setCategoryProfile(topN(categoryMap, 5));
        profile.setAuthorProfile(topN(authorMap, 5));
        profile.setBehaviorCount(behaviorCount);
        profile.setProfileLevel(calcLevel(behaviorCount));
        profile.setLastCalculatedAt(LocalDateTime.now());

        UserProfile exist = userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery()
                        .eq(UserProfile::getUserId, userId)
        );

        if (exist == null) {
            userProfileMapper.insert(profile);
        } else {
            profile.setId(exist.getId());
            userProfileMapper.updateById(profile);
        }
    }

    @Override
    public void calculateAll() {
        // 第一版：只算有行为的用户
        List<Long> userIds = behaviorLogMapper.selectList(
                Wrappers.<UserBehaviorLog>lambdaQuery()
                        .select(UserBehaviorLog::getUserId)
                        .groupBy(UserBehaviorLog::getUserId)
        ).stream().map(UserBehaviorLog::getUserId).distinct().collect(Collectors.toList());

        for (Long userId : userIds) {
            calculateByUserId(userId);
        }
    }

    /* ---------------- 私有方法 ---------------- */

    private Map<Long, Double> topN(Map<Long, Double> source, int n) {
        return source.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(n)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().doubleValue(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private int calcLevel(int count) {
        if (count < 5) return 0;
        if (count < 15) return 1;
        if (count < 50) return 2;
        return 3;
    }
}
