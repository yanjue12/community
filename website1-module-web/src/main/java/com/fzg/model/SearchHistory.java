package com.fzg.model;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 用户搜索历史表
 * search_history
 */
@Data
@TableName("search_history")
public class SearchHistory implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 搜索词
     */
    private String searchTerm;

    /**
     * 搜索时间
     */
    private Date searchedAt;

    /**
     * 最后搜索时间
     */
    private Date lastSearchedAt;

    private static final long serialVersionUID = 1L;
}