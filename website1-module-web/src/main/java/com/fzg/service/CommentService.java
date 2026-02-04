package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Comment;
import com.fzg.vo.CommentPageVO;
import com.fzg.vo.CommentVO;

public interface CommentService extends IService<Comment> {
    Boolean saveComment(CommentVO comment);

    CommentPageVO queryComList(Long articleId, Long lastId, Integer size);
    CommentPageVO queryChildComList(Long articleId, Long parentId, Integer size);
}
