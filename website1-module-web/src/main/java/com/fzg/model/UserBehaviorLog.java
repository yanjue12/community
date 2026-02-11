package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("user_behavior_log")
public class UserBehaviorLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long articleId;

    private Integer behaviorType;
    private Integer behaviorWeight;

    private Long tagId;
    private Long categoryId;
    private Long authorId;

    private LocalDateTime createAt;
}
