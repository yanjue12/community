package com.fzg.task;

import com.fzg.constant.RedisRecommendKey;
import com.fzg.service.UserProfileCalculateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserProfileRefreshTask {

    private static final String DIRTY_PROFILE_KEY = "user:profile:dirty";
    private static final long BATCH_SIZE = 100;

    private final RedisTemplate<String, String> redisTemplate;
    private final UserProfileCalculateService userProfileCalculateService;

    @Scheduled(fixedDelay = 60 * 1000)
    public void refreshDirtyProfiles() {
        try {
            List<String> userIds = redisTemplate.opsForSet().pop(DIRTY_PROFILE_KEY, BATCH_SIZE);
            if (userIds == null || userIds.isEmpty()) {
                return;
            }

            int success = 0;
            for (String userIdStr : userIds) {
                try {
                    Long userId = Long.valueOf(userIdStr);
                    userProfileCalculateService.calculateByUserId(userId);
                    redisTemplate.delete(RedisRecommendKey.userRecommendList(userId));
                    success++;
                } catch (Exception e) {
                    log.warn("用户画像重算失败, userId={}, err={}", userIdStr, e.getMessage());
                }
            }

            log.info("用户画像刷新完成, 本批处理={}, 成功={}", userIds.size(), success);
        } catch (Exception e) {
            log.error("用户画像刷新任务执行失败: {}", e.getMessage(), e);
        }
    }
}
