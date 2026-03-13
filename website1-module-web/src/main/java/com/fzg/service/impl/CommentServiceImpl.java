package com.fzg.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fzg.enums.EnumReturn;
import com.fzg.mapper.Articlemapper;
import com.fzg.mapper.CommentLikeRecordMapper;
import com.fzg.mapper.Commentmapper;
import com.fzg.mapper.Followmapper;
import com.fzg.mapper.UserMapper;
import com.fzg.model.Article;
import com.fzg.model.Comment;
import com.fzg.model.CommentLikeRecord;
import com.fzg.model.Follow;
import com.fzg.model.Result;
import com.fzg.model.User;
import com.fzg.model.UserPrivacy;
import com.fzg.service.CommentService;
import com.fzg.service.NotificationPublisher;
import com.fzg.service.UserPrivacyService;
import com.fzg.vo.CommentPageVO;
import com.fzg.vo.CommentVO;
import com.fzg.vo.LikeRequest;
import com.fzg.vo.RootCommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    @Autowired
    private CommentLikeRecordMapper commentLikeRecordMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
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
                
                // 检测@提及
                List<String> mentionedUsernames = extractMentions(comment.getContent());
                if (!mentionedUsernames.isEmpty()) {
                    // 查询文章标题用于@提及通知的上下文
                    Article article = articlemapper.selectById(comment.getArticleId());
                    String articleTitle = article != null ? article.getTitle() : "文章";
                    
                    // 处理@提及通知
                    processMentionNotifications(mentionedUsernames, comment.getUserId(), replierName, 
                                              "comment", commentEntity.getId(), comment.getContent(), articleTitle);
                }
                
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
            
            // 检测@提及
            List<String> mentionedUsernames = extractMentions(comment.getContent());
            if (!mentionedUsernames.isEmpty()) {
                // 处理@提及通知
                processMentionNotifications(mentionedUsernames, comment.getUserId(), commenterName, 
                                          "comment", commentEntity.getId(), comment.getContent(), articleTitle);
            }
            
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
    
    /**
     * 提取评论中的@用户名
     */
    private List<String> extractMentions(String content) {
        List<String> mentions = new ArrayList<>();
        if (content == null || content.trim().isEmpty()) {
            return mentions;
        }
        
        // 匹配@用户名的正则表达式，支持中文、英文、数字、下划线
        Pattern pattern = Pattern.compile("@([\\w\\u4e00-\\u9fa5]+)");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String username = matcher.group(1);
            if (!mentions.contains(username)) {
                mentions.add(username);
            }
        }
        
        return mentions;
    }
    
    /**
     * 处理@提及通知
     */
    private void processMentionNotifications(List<String> mentionedUsernames, Long mentionerId, 
                                           String mentionerName, String contentType, Long contentId, 
                                           String content, String contextTitle) {
        for (String username : mentionedUsernames) {
            // 根据用户名查找用户
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getNickname, username).or().eq(User::getUsername, username);
            User mentionedUser = userMapper.selectOne(wrapper);
            
            if (mentionedUser != null && !mentionedUser.getId().equals(mentionerId)) {
                // 发送@提及通知
                notificationPublisher.publishMentionNotification(
                        mentionedUser.getId(), mentionerId, contentType, 
                        contentId, content, mentionerName, contextTitle
                );
            }
        }
    }
    
    /**
     * 评论点赞/取消点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public Result commentLike(LikeRequest likeRequest) {
        Integer actionLike = likeRequest.getActionLike();
        Long userId = likeRequest.getUserId();
        Long commentId = likeRequest.getArticleId(); // 这里复用articleId字段作为commentId

        String lockKey = "comment_like_lock:" + userId + ":" + commentId;
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 3, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(locked)) {
            return Result.success(true);
        }

        try {
            LambdaQueryWrapper<CommentLikeRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CommentLikeRecord::getUserId, userId)
                   .eq(CommentLikeRecord::getCommentId, commentId);
            CommentLikeRecord record = commentLikeRecordMapper.selectOne(wrapper);

            String oldStatus = null;
            // 没有记录 第一次点赞 新增
            if (record == null) {
                if (actionLike == 1) {
                    CommentLikeRecord r = new CommentLikeRecord();
                    r.setUserId(userId);
                    r.setCommentId(commentId);
                    r.setStatus(String.valueOf(actionLike));
                    commentLikeRecordMapper.insert(r);
                    oldStatus = "0";
                } else {
                    // 用户取消点赞，但实际没有点赞，直接返回成功
                    return Result.success(true);
                }
            } else {
                // 有数据，取消点赞或者点赞
                oldStatus = record.getStatus();
                String newStatus = String.valueOf(actionLike);
                if (oldStatus.equals(newStatus)) {
                    return Result.success(true);
                }
                record.setStatus(newStatus);
                commentLikeRecordMapper.updateById(record);
            }

            // 只有状态真正改变 才更新评论数据库和发送通知
            if (oldStatus != null && !oldStatus.equals(String.valueOf(actionLike))) {
                // 更新评论点赞数（需要在Commentmapper中添加此方法）
                // commentmapper.updateCommentLikeCount(commentId, actionLike);

                // 点赞时发送通知
                if (actionLike == 1) {
                    Comment comment = baseMapper.selectById(commentId);
                    User liker = userMapper.selectById(userId);
                    
                    if (comment != null && liker != null) {
                        String likerName = liker.getNickname() != null ? liker.getNickname() : "匿名用户";
                        
                        // 获取文章标题
                        Article article = articlemapper.selectById(comment.getArticleId());
                        String articleTitle = article != null ? article.getTitle() : "未知文章";
                        
                        notificationPublisher.publishCommentLikeNotification(
                                comment.getUserId(), userId, commentId, 
                                comment.getContent(), likerName, articleTitle
                        );
                    }
                }
            }

            // 更新 Redis 缓存点赞状态
            String cacheKey = "comment_like_status:" + userId + ":" + commentId;
            if (actionLike == 1) {
                redisTemplate.opsForValue().set(cacheKey, "1", 7, TimeUnit.DAYS);
            } else {
                redisTemplate.delete(cacheKey);
            }
        } catch (Exception e) {
            log.error("评论点赞操作失败:{}", e.getMessage(), e);
            return Result.fail(EnumReturn.valueOf("评论点赞操作失败"));
        } finally {
            redisTemplate.delete(lockKey);
        }
        
        return Result.success(true);
    }






}
