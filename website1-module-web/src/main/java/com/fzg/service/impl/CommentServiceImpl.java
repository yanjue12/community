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
        //参数兜底（防止前端乱传）
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        // TODO 保存评论 并且发消息到消息队列，通知用户
        if (comment.getParentId() == 0) {
            // 一级评论
            comment.setRootId(0L);
        } else {
            // 回复评论
            // 如果前端没传 rootId，就查父评论
            if (comment.getRootId() == null || comment.getRootId() == 0) {
                Comment parent = baseMapper.selectById(comment.getParentId());
                if (parent == null) {
                    throw new RuntimeException("父评论不存在");
                }

                // 父评论是一级
                if (parent.getParentId() == 0) {
                    comment.setRootId(parent.getId());
                } else {
                    // 父评论是二级
                    comment.setRootId(parent.getRootId());
                }
            }
        }

        //插入评论
        int insert = baseMapper.insert(comment);
        if (insert <= 0) {
            return false;
        }

        //如果是回复，更新一级评论的 reply_count
        if (comment.getParentId() != 0) {
            baseMapper.incrementReplyCount(comment.getRootId());
        }

        return true;
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
