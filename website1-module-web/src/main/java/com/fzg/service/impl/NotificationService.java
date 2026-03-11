package com.fzg.service.impl;

import com.alibaba.fastjson.JSON;
import com.fzg.mapper.Notificationmapper;
import com.fzg.model.Article;
import com.fzg.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    @Autowired
    private Notificationmapper notificationMapper;

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
    ){

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

        if(extraData != null){
            notification.setExtraData(JSON.toJSONString(extraData));
        }

        notificationMapper.insert(notification);
    }


    @Transactional
    public void likeArticleNtfc(Long userId, Long articleId){

        likeRecordMapper.insert(...);

        Article article = articlemapper.selectById(articleId);

        if(!article.getUserId().equals(userId)){

            notificationService.createNotification(
                    article.getUserId(),
                    userId,
                    "user",
                    "like_article",
                    "文章被点赞",
                    "有人点赞了你的文章",
                    "article",
                    articleId,
                    "like_article_" + articleId,
                    null
            );
        }

    }

    public void follow(Long followerId, Long followingId){

        followMapper.insert(...);

        notificationService.createNotification(
                followingId,
                followerId,
                "user",
                "follow",
                "有人关注了你",
                "用户关注了你",
                "user",
                followerId,
                "follow_" + followerId,
                null
        );
    }

    @Transactional
    public void sendMessage(Long senderId, Long receiverId, String content){

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);

        messageMapper.insert(message);

        notificationService.createNotification(
                receiverId,
                senderId,
                "message",
                "private_message",
                "你收到一条私信",
                content,
                "message",
                message.getId(),
                "message_" + senderId,
                null
        );
    }


}