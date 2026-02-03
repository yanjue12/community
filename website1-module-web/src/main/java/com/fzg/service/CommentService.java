package com.fzg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fzg.model.Comment;

public interface CommentService extends IService<Comment> {
    Boolean saveComment(Comment comment);
}
