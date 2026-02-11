package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fzg.handler.JsonMapTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName("user_profile")
public class UserProfile {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;

    // key: tagId, value: weight
    @TableField(typeHandler = JsonMapTypeHandler.class)
    private Map<Long, Double> tagProfile;

    @TableField(typeHandler = JsonMapTypeHandler.class)
    private Map<Long, Double> categoryProfile;

    @TableField(typeHandler = JsonMapTypeHandler.class)
    private Map<Long, Double> authorProfile;


    private Integer behaviorCount;
    private Integer profileLevel;

    private LocalDateTime lastCalculatedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
