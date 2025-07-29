package com.fzg.constant;

public class RedisSolutionsKey {

    /**
     * 构建 Redis Key
     *
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @return Redis Key
     */
    public static String getSolutionsKey(Integer pageNum, Integer pageSize) {
        return String.format("solutions:page:%d:size:%d", pageNum, pageSize);
    }
}
