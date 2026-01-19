package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 收藏表
 * favorite
 */
@Data
public class Favorite implements Serializable {
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
     * 目标类型 1:文章 2:问题 3:代码
     */
    private String targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 收藏夹ID
     */
    private Long folderId;
    private String status;

    /**
     * 创建时间
     */
    private Date createdAt;
    private Date updatedAt;

    private static final long serialVersionUID = 1L;
}