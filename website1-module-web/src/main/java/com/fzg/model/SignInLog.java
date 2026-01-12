package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 签到记录表
 * sign_in_log
 */
@Data
public class SignInLog implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 签到日期
     */
    private Date date;

    /**
     * 连续签到天数
     */
    private Integer continuousDays;

    /**
     * 获得积分
     */
    private Integer scoreAward;

    /**
     * 创建时间
     */
    private Date createdAt;

    private static final long serialVersionUID = 1L;
}