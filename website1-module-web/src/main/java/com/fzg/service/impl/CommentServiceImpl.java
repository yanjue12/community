package com.fzg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.Commentmapper;
import com.fzg.mapper.Followmapper;
import com.fzg.model.Comment;
import com.fzg.model.Follow;
import com.fzg.model.UserPrivacy;
import com.fzg.service.CommentService;
import com.fzg.service.UserPrivacyService;
import com.fzg.vo.CommentPageVO;
import com.fzg.vo.CommentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl extends ServiceImpl<Commentmapper, Comment> implements CommentService {

    @Autowired
    private UserPrivacyService userPrivacyService;
    @Autowired
    private Followmapper followmapper;

    @Override
    public Boolean saveComment(CommentVO comment) {
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
        Comment commentEntity = new Comment();

        //判断隐私
        LambdaQueryWrapper<UserPrivacy> u = new LambdaQueryWrapper<>();
        u.eq(UserPrivacy::getUserId, comment.getUserId());
        UserPrivacy userPrivacy = userPrivacyService.getOne(u);
        String canComment = userPrivacy.getCanComment();
        if("0".equals(canComment)){
            //插入评论
            BeanUtils.copyProperties(comment, commentEntity);
            int insert = baseMapper.insert(commentEntity);
            if (insert <= 0) {
                return false;
            }
        } else if("1".equals(canComment)){
            if(comment.getUserId() == comment.getAuthorId()){
                //插入评论
                BeanUtils.copyProperties(comment, commentEntity);
                int insert = baseMapper.insert(commentEntity);
                if (insert <= 0) {
                    return false;
                }
            } else {
                return false;
            }
            } else if(("2".equals(canComment))){//粉丝可评论
                LambdaQueryWrapper<Follow> f = new LambdaQueryWrapper<>();
                f.eq(Follow::getFollowerId,comment.getUserId())
                        .eq(Follow::getFollowingId,comment.getAuthorId());
                Follow follow = followmapper.selectOne(f);
                if(null == follow){
                    return false;
                }
                BeanUtils.copyProperties(comment, commentEntity);
                int insert = baseMapper.insert(commentEntity);
                if (insert <= 0) {
                    return false;
                }
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
