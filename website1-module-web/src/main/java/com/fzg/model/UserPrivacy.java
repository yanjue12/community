package com.fzg.model;

import lombok.Data;

import java.util.Date;

@Data
public class UserPrivacy {

    // 隐私设置ID
    private Long id;

    // 用户ID
    private Long userId;

    // 邮箱可见性 0: 公开 1: 私密
    private String emailVisibility = "0";

    // 手机号可见性 0: 公开 1: 私密
    private String phoneVisibility = "0";

    // 个人主页可见性 0: 公开 1: 私密 2: 粉丝可见 3: 关注可见
    private String profileVisibility = "0";

    // 是否可评论 0: 全部可以 1: 仅自己评论 2: 粉丝可评论 3: 互相关注可评论
    private String canComment = "0";

    // 文章可见性 0: 公开 1: 私密 2: 粉丝可见 3: 互相关注可见
    private String articleVisibility = "0";

    // 喜欢列表是否隐藏 0: 显示 1: 隐藏
    private String likesHidden = "0";

    // 收藏列表是否隐藏 0: 显示 1: 隐藏
    private String favoritesHidden = "0";

    // 关注列表是否隐藏 0: 显示 1: 隐藏
    private String followListHidden = "0";

    // 粉丝列表是否隐藏 0: 显示 1: 隐藏
    private String followersListHidden = "0";

    // 互动允许私信 0: 所有人 1: 仅粉丝 2: 仅互相关注 3: 禁止
    private String allowPrivateMessage = "0";

    // 允许@提及 0: 是 1: 否
    private String allowMention = "0";

    // 新粉丝通知 0: 是 1: 否
    private String newFollowerNotification = "0";

    // 允许推荐作品 0: 是 1: 否
    private String allowRecommendation = "0";

    // 基于兴趣推荐内容 0: 是 1: 否
    private String interestBasedRecommendation = "0";

    // 数据分析 0: 是 1: 否
    private String dataAnalysis = "0";

    // 第三方数据共享 0: 是 1: 否
    private String thirdPartyDataSharing = "0";

    // 创建时间
    private Date createdAt;

    // 更新时间
    private Date updatedAt;
}