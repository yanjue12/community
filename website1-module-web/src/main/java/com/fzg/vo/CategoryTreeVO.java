package com.fzg.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类树形结构VO
 */
@Data
public class CategoryTreeVO {
    private Long id;
    private String categoryName;
    private Long parentId;
    private String status;
    private List<CategoryTreeVO> children;

}