package com.fzg.service.impl;

import com.fzg.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 通知发布器实现
 * 职责：发布通知事件到事件总线
 */
@Service
@RequiredArgsConstructor
public class NotificationPublisherImpl {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布通知事件
     */
    public void publishNotification(Long userId, Long fromUserId, String type, 
                                   String actionType, String title, String content,
                                   String targetType, Long targetId, String groupId,
                                   Map<String, Object> extraData) {
        NotificationEvent event = new NotificationEvent(
                this, userId, fromUserId, type, actionType, title, content,
                targetType, targetId, groupId, extraData
        );
        eventPublisher.publishEvent(event);
    }

    /**
     * 文章点赞通知
     */
    public void publishArticleLikeNotification(Long authorId, Long likerId, Long articleId, String articleTitle, String likerName) {
        Map<String, Object> extra = Map.of("articleTitle", articleTitle, "likerName", likerName);
        publishNotification(authorId, likerId, "user", "like_article",
                "文章获得点赞", likerName + "赞了你的文章《" + articleTitle + "》",
                "article", articleId, "like_article_" + articleId, extra);
    }

    /**
     * 评论点赞通知
     */
    public void publishCommentLikeNotification(Long commentAuthorId, Long likerId, Long commentId, String commentContent, String likerName, String articleTitle) {
        Map<String, Object> extra = Map.of("commentContent", commentContent, "likerName", likerName, "articleTitle", articleTitle);
        publishNotification(commentAuthorId, likerId, "user", "like_comment",
                "评论获得点赞", likerName + "赞了你在《" + articleTitle + "》中的评论",
                "comment", commentId, "like_comment_" + commentId, extra);
    }

    /**
     * 文章评论通知
     */
    public void publishArticleCommentNotification(Long authorId, Long commenterId, Long articleId, 
                                                 String articleTitle, Long commentId, String commentContent,
                                                 String commenterName) {
        Map<String, Object> extra = Map.of("articleTitle", articleTitle, "commentContent", commentContent, "commenterName", commenterName);
        publishNotification(authorId, commenterId, "user", "comment_article",
                "文章收到评论", commenterName + "评论了你的文章《" + articleTitle + "》",
                "article", articleId, "comment_article_" + articleId, extra);
    }

    /**
     * 评论回复通知
     */
    public void publishCommentReplyNotification(Long commentAuthorId, Long replierId, Long parentCommentId, 
                                               Long replyCommentId, String replyContent, String replierName) {
        Map<String, Object> extra = Map.of("replyContent", replyContent, "parentCommentId", parentCommentId, "replierName", replierName);
        publishNotification(commentAuthorId, replierId, "user", "reply_comment",
                "评论收到回复", replierName + "回复了你的评论",
                "comment", replyCommentId, "reply_comment_" + parentCommentId, extra);
    }

    /**
     * 关注通知
     */
    public void publishFollowNotification(Long followedUserId, Long followerId, String followerName) {
        Map<String, Object> extra = Map.of("followerName", followerName);
        publishNotification(followedUserId, followerId, "user", "follow",
                "新增粉丝", followerName + "关注了你",
                "user", followerId, "follow_" + followerId, extra);
    }

    /**
     * 文章收藏通知
     */
    public void publishArticleCollectNotification(Long authorId, Long collectorId, Long articleId, String articleTitle, String collectorName) {
        Map<String, Object> extra = Map.of("articleTitle", articleTitle, "collectorName", collectorName);
        publishNotification(authorId, collectorId, "user", "collect_article",
                "文章被收藏", collectorName + "收藏了你的文章《" + articleTitle + "》",
                "article", articleId, "collect_article_" + articleId, extra);
    }

    /**
     * 文章分享通知
     */
    public void publishArticleShareNotification(Long authorId, Long sharerId, Long articleId, String articleTitle, String sharerName) {
        Map<String, Object> extra = Map.of("articleTitle", articleTitle, "sharerName", sharerName);
        publishNotification(authorId, sharerId, "user", "share_article",
                "文章被分享", sharerName + "分享了你的文章《" + articleTitle + "》",
                "article", articleId, "share_article_" + articleId, extra);
    }

    /**
     * 新文章推送通知
     */
    public void publishNewArticleNotification(Long followerId, Long authorId, Long articleId, String articleTitle, String authorName) {
        Map<String, Object> extra = Map.of("articleTitle", articleTitle, "authorName", authorName);
        publishNotification(followerId, authorId, "user", "new_article",
                "关注的作者发布新文章", "你关注的" + authorName + "发布了新文章《" + articleTitle + "》",
                "article", articleId, "new_article_" + articleId, extra);
    }

    /**
     * @提及通知
     */
    public void publishMentionNotification(Long mentionedUserId, Long mentionerId, String contentType, 
                                          Long contentId, String content, String mentionerName, String contextTitle) {
        Map<String, Object> extra = Map.of("content", content, "mentionerName", mentionerName, "contextTitle", contextTitle);
        String contextDesc = "article".equals(contentType) ? "文章《" + contextTitle + "》" : contentType;
        publishNotification(mentionedUserId, mentionerId, "user", "mention",
                "有人@了你", mentionerName + "在" + contextDesc + "中提到了你",
                contentType, contentId, "mention_" + contentType + "_" + contentId, extra);
    }
}
