---
inclusion: manual
---

# 通知系统实现指南

## 系统架构

### 核心组件

1. **NotificationPublisher** - 通知发布器
   - 发布通知事件到Spring事件总线
   - 异步处理，不阻塞主业务流程
   - 提供各种场景的便捷方法

2. **NotificationEventListener** - 事件监听器
   - 异步监听通知事件
   - 将通知保存到数据库
   - 错误处理，不影响主业务

3. **NotificationService** - 通知服务
   - 通知查询、标记已读、删除等操作
   - Redis缓存未读数量
   - 支持分页、筛选

4. **BatchNotificationService** - 批量通知服务
   - 处理大量通知场景（如新文章推送）
   - 分批处理，避免内存溢出
   - 异步执行

5. **AsyncConfig** - 异步配置
   - 独立的线程池配置
   - 通知处理线程池：10核心线程，50最大线程
   - 批量处理线程池：5核心线程，20最大线程

## 使用场景

### 1. 点赞通知

```java
// 在 LikeRecordServiceImpl 中
if (actionLike == 1) {
    Article article = articlemapper.selectById(articleId);
    if (article != null) {
        notificationPublisher.publishArticleLikeNotification(
            article.getAuthorId(), userId, articleId, article.getTitle()
        );
    }
}
```

### 2. 关注通知

```java
// 在 FollowServiceImpl 中
if (actionFollow == 1) {
    // ... 保存关注关系
    notificationPublisher.publishFollowNotification(followingId, followerId);
}
```

### 3. 评论通知

```java
// 在 CommentServiceImpl 中
if (comment.getParentId() != 0) {
    // 回复评论
    Comment parentComment = baseMapper.selectById(comment.getParentId());
    if (parentComment != null) {
        notificationPublisher.publishCommentReplyNotification(
            parentComment.getUserId(), comment.getUserId(),
            comment.getParentId(), commentEntity.getId(), comment.getContent()
        );
    }
} else {
    // 一级评论
    notificationPublisher.publishArticleCommentNotification(
        comment.getAuthorId(), comment.getUserId(),
        comment.getArticleId(), "", commentEntity.getId(), comment.getContent()
    );
}
```

### 4. 新文章推送（批量）

```java
// 在 ArticleServiceImpl 中发布文章时
@Autowired
private BatchNotificationService batchNotificationService;

// 获取所有粉丝
List<Long> followerIds = followService.getFollowerIds(authorId);

// 异步推送给所有粉丝
batchNotificationService.notifyFollowersNewArticle(
    authorId, followerIds, articleId, articleTitle
);
```

### 5. 审核通知

```java
// 在审核通过时
auditNotificationService.notifyAuditPass(authorId, articleId, articleTitle);

// 在审核拒绝时
auditNotificationService.notifyAuditReject(authorId, articleId, articleTitle, reason);
```

## 性能优化

### 1. 异步处理
- 所有通知操作都是异步的，不阻塞主业务
- 使用独立的线程池，避免与其他业务竞争资源

### 2. 缓存策略
- 未读数量缓存1小时
- 使用Redis存储，减少数据库查询
- 操作后自动失效缓存

### 3. 批量处理
- 新文章推送等大量操作使用批量服务
- 分批插入数据库（每批500条）
- 避免一次性插入过多数据

### 4. 数据库索引
```sql
-- 关键索引
INDEX `idx_user_id` (`user_id`)
INDEX `idx_from_user_id` (`from_user_id`)
INDEX `idx_type` (`type`)
INDEX `idx_action_type` (`action_type`)
INDEX `idx_is_read` (`is_read`)
INDEX `idx_created_at` (`created_at`)
INDEX `idx_group_id` (`group_id`)
INDEX `idx_target` (`target_type`, `target_id`)
```

## 并发控制

### 1. 线程池配置
- **通知处理线程池**
  - 核心线程数: 10
  - 最大线程数: 50
  - 队列容量: 1000
  - 拒绝策略: CallerRunsPolicy（调用者运行）

- **批量处理线程池**
  - 核心线程数: 5
  - 最大线程数: 20
  - 队列容量: 500
  - 拒绝策略: DiscardPolicy（丢弃）

### 2. 事务管理
- 每个操作都使用 @Transactional 注解
- 异常时自动回滚
- 不影响主业务流程

### 3. 错误处理
- 异步处理中的异常不会抛出
- 所有异常都被捕获并记录日志
- 确保系统可用性

## API 接口

### 获取通知列表
```
GET /notification/list?userId=1&pageNum=1&pageSize=10&type=user&isRead=0
```

### 获取未读数量
```
GET /notification/unread/count?userId=1
```

### 获取各类型未读数量
```
GET /notification/unread/count/by-type?userId=1
```

### 标记为已读
```
PUT /notification/{id}/read?userId=1
```

### 批量标记为已读
```
PUT /notification/batch/read?userId=1
Body: [1, 2, 3]
```

### 全部标记为已读
```
PUT /notification/all/read?userId=1&type=user
```

### 删除通知
```
DELETE /notification/{id}?userId=1
```

### 批量删除
```
DELETE /notification/batch?userId=1
Body: [1, 2, 3]
```

### 清空已读通知
```
DELETE /notification/clear/read?userId=1
```

### 获取通知详情
```
GET /notification/{id}?userId=1
```

## 监控和日志

### 关键日志
- 通知事件处理：`log.debug("通知已保存: userId={}, actionType={}")`
- 批量处理进度：`log.debug("批量通知处理进度: {}/{}")`
- 批量处理完成：`log.info("批量通知处理完成: 总数={}, 耗时={}ms")`
- 新文章推送完成：`log.info("新文章推送完成: authorId={}, articleId={}, followerCount={}")`

### 性能指标
- 监控线程池队列深度
- 监控异步任务执行时间
- 监控缓存命中率

## 最佳实践

### 1. 何时使用异步通知
- ✅ 点赞、评论、关注等用户互动
- ✅ 新文章推送给粉丝
- ✅ 审核结果通知
- ❌ 不要用于需要立即反馈的操作

### 2. 何时使用批量通知
- ✅ 新文章推送给所有粉丝（可能数千人）
- ✅ 系统通知所有用户
- ❌ 不要用于单个用户的通知

### 3. 缓存策略
- 未读数量频繁查询，使用缓存
- 操作后立即失效缓存
- 定期检查缓存是否过期

### 4. 错误处理
- 异步处理中的异常不会影响主业务
- 所有异常都被记录，便于排查
- 定期检查日志中的错误

## 扩展建议

### 1. 消息队列集成
如果并发量很大，可以集成消息队列（如RabbitMQ、Kafka）：
```java
// 发布事件到消息队列
rabbitTemplate.convertAndSend("notification.exchange", "notification.key", event);
```

### 2. WebSocket推送
实时推送通知给前端：
```java
// 在事件监听器中
webSocketService.sendNotification(userId, notification);
```

### 3. 邮件/短信通知
重要通知可以发送邮件或短信：
```java
if ("important".equals(notification.getNotifyLevel())) {
    emailService.send(userId, notification);
}
```

### 4. 通知聚合
相同类型的通知可以聚合显示：
```java
// 使用 groupId 进行聚合
// 同一 groupId 的通知可以合并显示
```

## 故障排查

### 问题1：通知没有被保存
- 检查 NotificationEventListener 是否被正确注入
- 检查 @EnableAsync 是否在主应用类上
- 查看日志中是否有异常

### 问题2：缓存不一致
- 检查缓存失效逻辑是否正确
- 检查 Redis 连接是否正常
- 可以手动清除缓存重新加载

### 问题3：线程池队列满
- 增加线程池的最大线程数
- 增加队列容量
- 检查是否有长时间运行的任务

### 问题4：数据库性能下降
- 检查是否有缺失的索引
- 检查批量插入的批次大小
- 考虑使用分表分库
