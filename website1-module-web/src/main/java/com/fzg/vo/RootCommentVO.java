package com.fzg.vo;

import com.fzg.model.Comment;
import lombok.Data;

@Data
public class RootCommentVO {

    private Long rootId;                 // 一级评论ID
    private Comment rootComment;          // 可能为 null（已删除）
    private Boolean rootDeleted;           // true = 已删除
    private Integer replyCount;            // 二级评论数量
}