package com.fzg.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserPrivacy implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 隐私设置ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 邮箱可见性 0: 公开 1: 私密
     */
    private String emailVisibility;

    /**
     * 手机号可见性 0: 公开 1: 私密
     */
    private String phoneVisibility;

    /**
     * 个人主页可见性 0: 公开 1: 私密 2: 粉丝可见 3: 关注可见
     */
    private String profileVisibility;

    /**
     * 是否可评论 0: 全部可以 1：仅自己评论 2：粉丝可评论 3：互相关注可评论
     */
    private String canComment;

    /**
     * 文章可见性 0：公开 1：私密 2：粉丝可见 3：互相关注可见
     */
    private String articleVisibility;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}