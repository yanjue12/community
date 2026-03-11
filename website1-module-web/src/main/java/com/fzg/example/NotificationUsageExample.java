package com.fzg.example;

import com.fzg.service.impl.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 通知服务使用示例
 * 展示如何在各个业务场景中调用通知服务
 */
@Service
public class NotificationUsageExample {

    @Autowired
    private NotificationService notificationService;

    /**
     * 示例1：用户点赞文章时发送通知
     */
    public void exampleArticleLike(Long userId, Long articleId) {
        // 1. 执行点赞业务逻辑
        // likeRecordMapper.insert(likeRecord);
        
        // 2. 获取文章信息
        // Article article = articleMapper.selectById(articleId);
        
        // 3. 发送通知给文章作者
        Long authorId = 123L; // article.getAuthorId();
        String articleTitle = "示例文章标题"; // article.getTitle();
        
        notificationService.notifyArticleLike(authorId, userId, articleId, articleTitle);
    }

    /**
     * 示例2：用户评论文章时发送通知
     */
    public void exampleArticleComment(Long userId, Long articleId, Long commentId) {
        // 1. 插入评论
        // commentMapper.insert(comment);
        
        // 2. 获取文章和评论信息
        // Article article = articleMapper.selectById(articleId);
        // Comment comment = commentMapper.selectById(commentId);
        
        // 3. 发送通知给文章作者
        Long authorId = 123L;
        String articleTitle = "示例文章标题";
        String commentContent = "这是评论内容";
        
        notificationService.notifyArticleComment(
            authorId, 
            userId, 
            articleId, 
            articleTitle, 
            commentId, 
            commentContent
        );
    }

    /**
     * 示例3：用户回复评论时发送通知
     */
    public void exampleCommentReply(Long userId, Long parentCommentId, Long replyCommentId) {
        // 1. 插入回复评论
        // commentMapper.insert(replyComment);
        
        // 2. 获取父评论信息
        // Comment parentComment = commentMapper.selectById(parentCommentId);
        
        // 3. 发送通知给被回复的用户
        Long commentAuthorId = 456L; // parentComment.getUserId();
        String replyContent = "这是回复内容";
        
        notificationService.notifyCommentReply(
            commentAuthorId, 
            userId, 
            parentCommentId, 
            replyCommentId, 
            replyContent
        );
    }

    /**
     * 示例4：用户点赞评论时发送通知
     */
    public void exampleCommentLike(Long userId, Long commentId) {
        // 1. 执行点赞业务逻辑
        // commentLikeRecordMapper.insert(likeRecord);
        
        // 2. 获取评论信息
        // Comment comment = commentMapper.selectById(commentId);
        
        // 3. 发送通知给评论作者
        Long commentAuthorId = 456L; // comment.getUserId();
        String commentContent = "这是评论内容";
        
        notificationService.notifyCommentLike(commentAuthorId, userId, commentId, commentContent);
    }

    /**
     * 示例5：用户关注时发送通知
     */
    public void exampleFollow(Long followerId, Long followedUserId) {
        // 1. 插入关注记录
        // followMapper.insert(follow);
        
        // 2. 发送通知给被关注的用户
        notificationService.notifyFollow(followedUserId, followerId);
    }

    /**
     * 示例6：用户收藏文章时发送通知
     */
    public void exampleArticleCollect(Long userId, Long articleId) {
        // 1. 插入收藏记录
        // favoriteMapper.insert(favorite);
        
        // 2. 获取文章信息
        // Article article = articleMapper.selectById(articleId);
        
        // 3. 发送通知给文章作者
        Long authorId = 123L;
        String articleTitle = "示例文章标题";
        
        notificationService.notifyArticleCollect(authorId, userId, articleId, articleTitle);
    }

    /**
     * 示例7：用户分享文章时发送通知
     */
    public void exampleArticleShare(Long userId, Long articleId) {
        // 1. 记录分享行为
        // shareRecordMapper.insert(shareRecord);
        
        // 2. 获取文章信息
        // Article article = articleMapper.selectById(articleId);
        
        // 3. 发送通知给文章作者
        Long authorId = 123L;
        String articleTitle = "示例文章标题";
        
        notificationService.notifyArticleShare(authorId, userId, articleId, articleTitle);
    }

    /**
     * 示例8：检测@提及并发送通知
     */
    public void exampleMention(Long userId, String content, String contentType, Long contentId) {
        // 1. 解析内容中的@用户
        // 假设content = "这是一条评论 @张三 @李四"
        // List<String> mentionedUsernames = parseMentions(content);
        
        // 2. 查询被@的用户ID
        // List<User> mentionedUsers = userMapper.selectByUsernames(mentionedUsernames);
        
        // 3. 给每个被@的用户发送通知
        // for (User mentionedUser : mentionedUsers) {
        //     notificationService.notifyMention(
        //         mentionedUser.getId(),
        //         userId,
        //         contentType, // "article" 或 "comment"
        //         contentId,
        //         content
        //     );
        // }
    }

    /**
     * 示例9：关注的作者发布新文章时通知粉丝
     */
    public void exampleNewArticleNotifyFollowers(Long authorId, Long articleId) {
        // 1. 发布文章
        // articleMapper.insert(article);
        
        // 2. 获取作者的所有粉丝
        // List<Follow> followers = followMapper.selectList(
        //     new LambdaQueryWrapper<Follow>().eq(Follow::getFollowingId, authorId)
        // );
        
        // 3. 给每个粉丝发送通知
        String articleTitle = "新文章标题";
        // for (Follow follow : followers) {
        //     notificationService.notifyFollowerNewArticle(
        //         follow.getFollowerId(),
        //         authorId,
        //         articleId,
        //         articleTitle
        //     );
        // }
    }

    /**
     * 示例10：发送系统通知
     */
    public void exampleSystemNotification(Long userId) {
        // 发送普通系统通知
        notificationService.notifySystem(
            userId,
            "系统维护通知",
            "系统将于今晚22:00-23:00进行维护，请提前保存数据",
            "normal"
        );
        
        // 发送重要系统通知
        notificationService.notifySystem(
            userId,
            "账号安全提醒",
            "检测到您的账号在异地登录，请及时修改密码",
            "important"
        );
    }

    /**
     * 辅助方法：解析@提及
     */
    private java.util.List<String> parseMentions(String content) {
        java.util.List<String> mentions = new java.util.ArrayList<>();
        // 使用正则表达式提取@用户名
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("@([\\w\\u4e00-\\u9fa5]+)");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        return mentions;
    }
}
