package com.fzg.model;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 热门搜索表
 * search_hot
 */
@Data
public class SearchHot implements Serializable {
    private Integer id;

    /**
     * 搜索词
     */
    private String searchTerm;

    /**
     * 搜索次数
     */
    private Integer searchCount;

    private Date updatedAt;

    private Date createdAt;

    private static final long serialVersionUID = 1L;
}