package com.fzg.vo;

import lombok.Data;

import java.util.Date;

@Data
public class FollowVO {
    /**
     * 关注者ID
     */
    private Long followerId;

    /**
     * 被关注者ID
     */
    private Long followingId;

    /**
     * 创建时间
     */
    private Date createdAt;
    private Integer actionFollow;//1 关注 0 取消关注
}
