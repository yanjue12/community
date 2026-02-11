package com.fzg.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BehaviorTypeEnum {

    VIEW(1, 1,  "浏览"),
    LIKE(2, 5,  "点赞"),
    COLLECT(3, 8, "收藏"),
    COMMENT(4, 6, "评论"),
    SHARE(5, 4, "分享");

    private final int code;
    private final int baseWeight;
    private final String desc;

    public static BehaviorTypeEnum of(int code) {
        for (BehaviorTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        throw new IllegalArgumentException("非法行为类型");
    }
}
