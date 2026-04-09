package com.fzg.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户答卷
 */
@Data
@TableName("paper_attempt")
public class PaperAttempt {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private Long userId;
    private Date startTime;
    private Date submitTime;
    private Integer totalScore;
    private Integer status; // 0进行中 1已交卷
}
