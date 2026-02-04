package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Comment;
import com.fzg.vo.CommentPageVO;

public interface CommentService extends IService<Comment> {
    Boolean saveComment(Comment comment);

    CommentPageVO queryComList(Long articleId, Long lastId, Integer size);
    CommentPageVO queryChildComList(Long articleId, Long parentId, Integer size);
}
