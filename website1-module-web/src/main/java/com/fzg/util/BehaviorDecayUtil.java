package com.fzg.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 时间衰减工具
 */
public class BehaviorDecayUtil {

    public static double decay(LocalDateTime time) {
        if (time == null) {
            return 0.0;
        }
        long days = ChronoUnit.DAYS.between(time, LocalDateTime.now());

        if (days <= 7) return 1.0;
        if (days <= 30) return 0.7;
        if (days <= 90) return 0.3;
        return 0.0;
    }
}
