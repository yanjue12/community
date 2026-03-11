package com.fzg.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * 通知事件 - 用于异步处理通知
 */
@Data
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
}
