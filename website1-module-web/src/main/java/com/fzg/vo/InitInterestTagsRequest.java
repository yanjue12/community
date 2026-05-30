package com.fzg.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 引导式注册：初始化用户兴趣标签请求
 * 注册后让用户主动选择技术标签，解决推荐系统冷启动问题
 */
@Schema(name = "初始化兴趣标签请求")
@Data
public class InitInterestTagsRequest {

    /** 用户ID */
    private Long userId;

    /** 用户选中的标签ID列表（建议 3~10 个） */
    private List<Long> tagIds;
}
