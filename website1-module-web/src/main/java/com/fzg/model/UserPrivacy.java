package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_privacy")
public class UserPrivacy {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String emailVisibility;

    private String phoneVisibility;

    private String profileVisibility;

    private String canComment;

    private String articleVisibility;

    private String likesHidden;

    private String favoritesHidden;

    private String followListHidden;

    private String followersListHidden;

    private String allowPrivateMessage;

    private String allowMention;

    private String newFollowerNotification;

    private String allowRecommendation;

    private String interestBasedRecommendation;

    private String dataAnalysis;

    private String thirdPartyDataSharing;

    private Date createdAt;

    private Date updatedAt;
}
