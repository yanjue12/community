package com.fzg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.Commentmapper;
import com.fzg.model.Comment;
import com.fzg.service.CommentService;
import com.fzg.vo.CommentPageVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<Commentmapper, Comment> implements CommentService {

    @Override
    public Boolean saveComment(Comment comment) {
        // TODO 保存评论 并且发消息到消息队列，通知用户
        int insert = baseMapper.insert(comment);

        return insert > 0;
    }

    @Override
    public CommentPageVO queryComList(Long articleId, Long lastId, Integer size) {
        List<Comment> list = baseMapper.selectRootCommentPage(articleId, lastId, size);

        CommentPageVO vo = new CommentPageVO();
        vo.setList(list);

        if (!list.isEmpty()) {
            vo.setLastId(list.get(list.size() - 1).getId());
        }

        vo.setHasMore(list.size() == size);
        return vo;
    }

    @Override
    public CommentPageVO queryChildComList(Long rootId, Long lastId, Integer size) {
        List<Comment> list =
                baseMapper.selectChildCommentPage(rootId, lastId, size);

        CommentPageVO vo = new CommentPageVO();
        vo.setList(list);

        if (!list.isEmpty()) {
            vo.setLastId(list.get(list.size() - 1).getId());
        }

        vo.setHasMore(list.size() == size);
        return vo;
    }
}
