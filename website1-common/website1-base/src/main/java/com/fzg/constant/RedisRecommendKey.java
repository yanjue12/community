package com.fzg.constant;

public class RedisRecommendKey {

    // 最近曝光文章
    public static String userExposeSet(Long userId) {
        return "rec:expose:" + userId;
    }

}
