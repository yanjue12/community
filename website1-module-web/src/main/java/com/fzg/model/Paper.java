package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 试卷
 */
@Data
@TableName("paper")
public class Paper {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String title;
    private Integer totalScore;
    private Integer timeLimit; // 分钟
    private Integer status; // 0草稿 1发布
    private Date createdAt;
    private Date updatedAt;
}
