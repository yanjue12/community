package com.fzg.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 实时动态数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeActivityDTO {
    
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;

    private Long userId;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 动作类型
     */
    private String actionType;
    
    /**
     * 动作描述
     */
    private String actionDescription;
    
    /**
     * 目标标题（文章标题等）
     */
    private String targetTitle;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 时间描述（如：5分钟前）
     */
    private String timeDescription;
    
    /**
     * 状态（已发布、待审核等）
     */
    private String status;
}