package com.fzg.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fzg.mapper.Notificationmapper;
import com.fzg.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 通知服务
 */
@Service
public class NotificationService {

    @Autowired
    private Notificationmapper notificationMapper;

    /**
     * 创建通知（通用方法）
     */
    @Transactional(rollbackFor = Exception.class)
    public void createNotification(
            Long userId,
            Long fromUserId,
            String type,
            String actionType,
            String title,
            String content,
            String targetType,
            Long targetId,
            String groupId,
            Object extraData
    ) {
        // 不给自己发通知
        if (userId.equals(fromUserId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setActionType(actionType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setGroupId(groupId);
        notification.setIsRead("0");
        notification.setIsDeleted("0");
        notification.setNotifyLevel("normal");
        notification.setCreatedAt(new Date());

        if (extraData != null) {
            notification.setExtraData(JSON.toJSONString(extraData));
        }

        notificationMapper.insert(notification);
    }

    /**
     * 文章被点赞通知
     */
    public void notifyArticleLike(Long authorId, Long likerId, Long articleId, String articleTitle) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("articleTitle", articleTitle);
        
        createNotification(
                authorId,
                likerId,
                "user",
                "like_article",
                "文章获得点赞",
                "赞了你的文章《" + articleTitle + "》",
                "article",
                articleId,
                "like_article_" + articleId,
                extra
        );
    }

    /**
     * 评论被点赞通知
     */
    public void notifyCommentLike(Long commentAuthorId, Long likerId, Long commentId, String commentContent) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("commentContent", commentContent);
        
        createNotification(
                commentAuthorId,
                likerId,
                "user",
                "like_comment",
                "评论获得点赞",
                "赞了你的评论",
                "comment",
                commentId,
                "like_comment_" + commentId,
                extra
        );
    }

    /**
     * 文章被评论通知
     */
    public void notifyArticleComment(Long authorId, Long commenterId, Long articleId, String articleTitle, Long commentId, String commentContent) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("articleTitle", articleTitle);
        extra.put("commentContent", commentContent);
        
        createNotification(
                authorId,
                commenterId,
                "user",
                "comment_article",
                "文章收到评论",
                "评论了你的文章《" + articleTitle + "》",
                "article",
                articleId,
                "comment_article_" + articleId,
                extra
        );
    }

    /**
     * 评论被回复通知
     */
    public void notifyCommentReply(Long commentAuthorId, Long replierId, Long parentCommentId, Long replyCommentId, String replyContent) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("replyContent", replyContent);
        extra.put("parentCommentId", parentCommentId);
        
        createNotification(
                commentAuthorId,
                replierId,
                "user",
                "reply_comment",
                "评论收到回复",
                "回复了你的评论",
                "comment",
                replyCommentId,
                "reply_comment_" + parentCommentId,
                extra
        );
    }

    /**
     * @提及通知
     */
    public void notifyMention(Long mentionedUserId, Long mentionerId, String contentType, Long contentId, String content) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("content", content);
        
        createNotification(
                mentionedUserId,
                mentionerId,
                "user",
                "mention",
                "有人@了你",
                "在" + contentType + "中提到了你",
                contentType,
                contentId,
                "mention_" + contentType + "_" + contentId,
                extra
        );
    }

    /**
     * 关注通知
     */
    public void notifyFollow(Long followedUserId, Long followerId) {
        createNotification(
                followedUserId,
                followerId,
                "user",
                "follow",
                "新增粉丝",
                "关注了你",
                "user",
                followerId,
                "follow_" + followerId,
                null
        );
    }

    /**
     * 文章被收藏通知
     */
    public void notifyArticleCollect(Long authorId, Long collectorId, Long articleId, String articleTitle) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("articleTitle", articleTitle);
        
        createNotification(
                authorId,
                collectorId,
                "user",
                "collect_article",
                "文章被收藏",
                "收藏了你的文章《" + articleTitle + "》",
                "article",
                articleId,
                "collect_article_" + articleId,
                extra
        );
    }

    /**
     * 文章被分享通知
     */
    public void notifyArticleShare(Long authorId, Long sharerId, Long articleId, String articleTitle) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("articleTitle", articleTitle);
        
        createNotification(
                authorId,
                sharerId,
                "user",
                "share_article",
                "文章被分享",
                "分享了你的文章《" + articleTitle + "》",
                "article",
                articleId,
                "share_article_" + articleId,
                extra
        );
    }

    /**
     * 关注的作者发布新文章通知
     */
    public void notifyFollowerNewArticle(Long followerId, Long authorId, Long articleId, String articleTitle) {
        Map<String, Object> extra = new HashMap<>();
        extra.put("articleTitle", articleTitle);
        
        createNotification(
                followerId,
                authorId,
                "user",
                "new_article",
                "关注的作者发布新文章",
                "发布了新文章《" + articleTitle + "》",
                "article",
                articleId,
                "new_article_" + articleId,
                extra
        );
    }

    /**
     * 系统通知
     */
    public void notifySystem(Long userId, String title, String content, String level) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("system");
        notification.setActionType("system_notice");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead("0");
        notification.setIsDeleted("0");
        notification.setNotifyLevel(level != null ? level : "normal");
        notification.setCreatedAt(new Date());
        
        notificationMapper.insert(notification);
    }

    /**
     * 批量标记为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public int markAsRead(Long userId, java.util.List<Long> notificationIds) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .in(Notification::getId, notificationIds)
               .eq(Notification::getIsRead, "0");
        
        Notification update = new Notification();
        update.setIsRead("1");
        update.setReadAt(new Date());
        
        return notificationMapper.update(update, wrapper);
    }

    /**
     * 全部标记为已读
     */
    @Transactional(rollbackFor = Exception.class)
    public int markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, "0");
        
        Notification update = new Notification();
        update.setIsRead("1");
        update.setReadAt(new Date());
        
        return notificationMapper.update(update, wrapper);
    }

    /**
     * 获取未读数量
     */
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, "0")
               .eq(Notification::getIsDeleted, "0");
        return notificationMapper.selectCount(wrapper);
    }

    /**
     * 删除通知
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteNotification(Long userId, Long notificationId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getId, notificationId);
        
        Notification update = new Notification();
        update.setIsDeleted("1");
        
        return notificationMapper.update(update, wrapper);
    }
}