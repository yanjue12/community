package com.fzg.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * 通知事件 - 用于异步处理通知
 */
public class NotificationEvent extends ApplicationEvent {
    private Long userId;
    private Long fromUserId;
    private String type;
    private String actionType;
    private String title;
    private String content;
    private String targetType;
    private Long targetId;
    private String groupId;
    private Map<String, Object> extraData;

    public NotificationEvent(Object source, Long userId, Long fromUserId, String type, 
                            String actionType, String title, String content, 
                            String targetType, Long targetId, String groupId, 
                            Map<String, Object> extraData) {
        super(source);
        this.userId = userId;
        this.fromUserId = fromUserId;
        this.type = type;
        this.actionType = actionType;
        this.title = title;
        this.content = content;
        this.targetType = targetType;
        this.targetId = targetId;
        this.groupId = groupId;
        this.extraData = extraData;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
