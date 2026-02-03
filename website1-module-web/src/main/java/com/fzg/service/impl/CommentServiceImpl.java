package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.Commentmapper;
import com.fzg.model.Comment;
import com.fzg.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<Commentmapper, Comment> implements CommentService {

    @Override
    public Boolean saveComment(Comment comment) {
        // TODO 保存评论 并且发消息到消息队列，通知用户
        int insert = baseMapper.insert(comment);

        return insert > 0;
    }
}
