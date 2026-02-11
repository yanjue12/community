package com.fzg.constant;

public class RedisRecommendKey {

    // 最近曝光文章
    public static String userExposeSet(Long userId) {
        return "rec:expose:" + userId;
    }


    /**
     * 用户推荐池
     * rec:user:1001:list
     */
    public static String userRecommendList(Long userId) {
        return "rec:user:" + userId + ":list";
    }

}
