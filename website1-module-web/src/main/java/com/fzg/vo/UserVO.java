package com.fzg.vo;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class UserVO {
    private String userId;
    private String nickname;
    private String avatar;
    private String signature; // 个性签名
    private String location;
    private Integer topicCount; //发帖数
    private Integer commentCount;
    private Integer followCount;
    private Integer followingCount;//关注数
    private Integer collectionCount;
    private String coverImages;

    private Integer activeScore;//活跃度


}
