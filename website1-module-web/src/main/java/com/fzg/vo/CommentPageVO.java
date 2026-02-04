package com.fzg.vo;

import com.fzg.model.Comment;
import lombok.Data;

import java.util.List;

@Data
public class CommentPageVO<T> {
    private List<T> list;
    private Long lastId;   // 下次请求用
    private Boolean hasMore;
}
