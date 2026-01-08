package com.fzg.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户收藏夹表
 * favorite_folder
 */
@Data
public class FavoriteFolder implements Serializable {
    /**
     * 收藏夹ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏夹名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 是否公开 0:私密 1:公开
     */
    private String isPublic;

    /**
     * 是否为默认收藏夹
     */
    private String isDefault;

    /**
     * 收藏项数量
     */
    private Integer itemCount;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updated_at;

    private static final long serialVersionUID = 1L;
}