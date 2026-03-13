package com.fzg.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.Commentmapper;
import com.fzg.mapper.Followmapper;
import com.fzg.mapper.UserMapper;
import com.fzg.model.Article;
import com.fzg.model.Comment;
import com.fzg.model.Follow;
import com.fzg.model.User;
import com.fzg.model.UserPrivacy;
import com.fzg.service.CommentService;
import com.fzg.service.NotificationPublisher;
import com.fzg.service.UserPrivacyService;
import com.fzg.vo.CommentPageVO;
import com.fzg.vo.CommentVO;
import com.fzg.vo.RootCommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<Commentmapper, Comment> implements CommentService {

    @Autowired
    private UserPrivacyService userPrivacyService;
    @Autowired
    private Followmapper followmapper;
    @Autowired
    private Articlemapper articlemapper;
    @Autowired
    private UserMapper userMapper;
    private final NotificationPublisher notificationPublisher;

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
        u.eq(UserPrivacy::getUserId, comment.getAuthorId());
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
            
            // 发送回复通知
            Comment parentComment = baseMapper.selectById(comment.getParentId());
            if (parentComment != null) {
                // 查询回复用户信息
                User replier = userMapper.selectById(comment.getUserId());
                String replierName = replier != null ? replier.getNickname() : "用户";
                
                notificationPublisher.publishCommentReplyNotification(
                        parentComment.getUserId(), comment.getUserId(), 
                        comment.getParentId(), commentEntity.getId(), comment.getContent(),
                        replierName
                );
            }
        } else {
            // 一级评论，发送文章评论通知
            // 查询文章信息
            Article article = articlemapper.selectById(comment.getArticleId());
            String articleTitle = article != null ? article.getTitle() : "文章";
            
            // 查询评论用户信息
            User commenter = userMapper.selectById(comment.getUserId());
            String commenterName = commenter != null ? commenter.getNickname() : "用户";
            
            notificationPublisher.publishArticleCommentNotification(
                    comment.getAuthorId(), comment.getUserId(),
                    comment.getArticleId(), articleTitle, commentEntity.getId(), comment.getContent(),
                    commenterName
            );
        }

        return true;
    }

    @Override
    public CommentPageVO<RootCommentVO> queryComList(Long articleId, Long lastId, Integer size) {
        //查应该出现的 rootId（关键）
        List<Long> rootIds = baseMapper.selectRootIdsForPage(articleId, lastId, size + 1);
        log.info("rootIds:{}", JSON.toJSONString(rootIds));
        boolean hasMore = rootIds.size() > size;
        if (hasMore) {
            rootIds = rootIds.subList(0, size);
        }
        if(CollectionUtils.isEmpty(rootIds)){
            CommentPageVO<RootCommentVO> page = new CommentPageVO<>();
            page.setHasMore(hasMore);
            page.setList(new ArrayList<>());
            page.setLastId(0L);
            return page;
        }

        // 批量查一级评论（可能部分被删）
        List<Comment> roots = baseMapper.selectRootsByIds(rootIds);
        if(CollectionUtils.isEmpty( roots)){
            CommentPageVO<RootCommentVO> page = new CommentPageVO<>();
            page.setHasMore(hasMore);
            page.setList(new ArrayList<>());
            page.setLastId(0L);
            return page;
        }
        log.info("批量查出的一级评论");
        Map<Long, Comment> rootMap = roots.stream()
                .collect(Collectors.toMap(Comment::getId, c -> c));

        //组装 VO
        List<RootCommentVO> voList = new ArrayList<>();

        for (Long rootId : rootIds) {
            RootCommentVO vo = new RootCommentVO();
            vo.setRootId(rootId);

            Comment root = rootMap.get(rootId);
            if (root == null || !"1".equals(root.getStatus())) {
                vo.setRootDeleted(true);
                vo.setRootComment(null);
                vo.setReplyCount(
                        baseMapper.countChildByRootId(rootId)
                );
            } else {
                vo.setRootDeleted(false);
                vo.setRootComment(root);
                vo.setReplyCount(root.getReplyCount());
            }

            voList.add(vo);
        }

        CommentPageVO<RootCommentVO> page = new CommentPageVO<>();
        page.setList(voList);
        page.setHasMore(hasMore);
        page.setLastId(rootIds.isEmpty() ? null : rootIds.get(rootIds.size() - 1));
        return page;
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
