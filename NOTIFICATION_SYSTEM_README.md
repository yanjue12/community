# 高可用消息通知系统实现

## 📋 项目概述

这是一个为程序员管理系统设计的高可用、抗并发的消息通知系统。支持点赞、收藏、评论、关注、审核等多种通知场景。

## 🏗️ 系统架构

### 核心组件

```
┌─────────────────────────────────────────────────────────────┐
│                    业务服务层                                 │
│  (LikeService, FollowService, CommentService, etc.)         │
└────────────────────┬────────────────────────────────────────┘
                     │ 发布事件
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              NotificationPublisher                           │
│              (通知发布器)                                     │
└────────────────────┬────────────────────────────────────────┘
                     │ 发布事件
                     ▼
┌─────────────────────────────────────────────────────────────┐
│         Spring Event Bus (事件总线)                          │
└────────────────────┬────────────────────────────────────────┘
                     │ 异步监听
                     ▼
┌─────────────────────────────────────────────────────────────┐
│         NotificationEventListener                           │
│         (事件监听器 - 异步处理)                              │
└────────────────────┬────────────────────────────────────────┘
                     │ 保存
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              数据库 + Redis缓存                              │
│         (Notification表 + 未读数量缓存)                      │
└─────────────────────────────────────────────────────────────┘
```

### 关键特性

✅ **异步处理** - 不阻塞主业务流程
✅ **高并发** - 独立线程池，支持高并发
✅ **缓存优化** - Redis缓存未读数量
✅ **批量处理** - 支持大量通知场景
✅ **错误隔离** - 异常不影响主业务
✅ **可扩展** - 易于集成新的通知类型

## 📁 文件结构

```
website1-module-web/src/main/java/com/fzg/
├── event/
│   └── NotificationEvent.java              # 通知事件
├── listener/
│   └── NotificationEventListener.java      # 事件监听器
├── config/
│   └── AsyncConfig.java                    # 异步配置
├── service/
│   ├── NotificationService.java            # 通知服务
│   ├── NotificationPublisher.java          # 通知发布器
│   ├── BatchNotificationService.java       # 批量通知服务
│   ├── AuditNotificationService.java       # 审核通知服务
│   ├── NotificationIntegrationExample.java # 集成示例
│   └── impl/
│       ├── LikeRecordServiceImpl.java       # 已集成
│       ├── FollowServiceImpl.java           # 已集成
│       └── CommentServiceImpl.java          # 已集成
└── controller/
    └── NotificationController.java         # 通知API
```

## 🚀 快速开始

### 1. 数据库初始化

```sql
-- 执行 notification_table.sql
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `from_user_id` BIGINT NULL COMMENT '发送用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型',
    `action_type` VARCHAR(50) NOT NULL COMMENT '动作类型',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NULL COMMENT '内容',
    `target_type` VARCHAR(50) NULL COMMENT '目标类型',
    `target_id` BIGINT NULL COMMENT '目标ID',
    `parent_id` BIGINT NULL COMMENT '父级ID',
    `source_id` BIGINT NULL COMMENT '来源对象ID',
    `source_type` VARCHAR(50) NULL COMMENT '来源对象类型',
    `group_id` VARCHAR(100) NULL COMMENT '通知聚合ID',
    `extra_data` TEXT NULL COMMENT '额外数据',
    `is_read` VARCHAR(1) DEFAULT '0' COMMENT '是否已读',
    `is_deleted` VARCHAR(1) DEFAULT '0' COMMENT '是否删除',
    `notify_level` VARCHAR(20) DEFAULT 'normal' COMMENT '通知级别',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `read_at` DATETIME NULL COMMENT '阅读时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_from_user_id` (`from_user_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_action_type` (`action_type`),
    INDEX `idx_is_read` (`is_read`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_group_id` (`group_id`),
    INDEX `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';
```

### 2. 配置检查

确保以下配置已启用：

```yaml
# application.yml
spring:
  redis:
    host: localhost
    port: 6379
  task:
    execution:
      pool:
        core-size: 10
        max-size: 50
```

### 3. 主应用类配置

```java
@SpringBootApplication
@EnableAsync  // 启用异步处理
public class Website1ModuleWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(Website1ModuleWebApplication.class, args);
    }
}
```

## 💡 使用示例

### 示例1: 点赞通知

```java
// 在 LikeRecordServiceImpl 中已集成
if (actionLike == 1) {
    Article article = articlemapper.selectById(articleId);
    if (article != null) {
        notificationPublisher.publishArticleLikeNotification(
            article.getAuthorId(), userId, articleId, article.getTitle()
        );
    }
}
```

### 示例2: 关注通知

```java
// 在 FollowServiceImpl 中已集成
if (actionFollow == 1) {
    // ... 保存关注关系
    notificationPublisher.publishFollowNotification(followingId, followerId);
}
```

### 示例3: 新文章推送

```java
// 在 ArticleService 中使用
public void publishArticle(Article article) {
    save(article);
    
    // 获取所有粉丝
    List<Long> followerIds = followService.getFollowerIds(article.getAuthorId());
    
    // 异步推送给所有粉丝
    batchNotificationService.notifyFollowersNewArticle(
        article.getAuthorId(), followerIds, article.getId(), article.getTitle()
    );
}
```

## 📊 性能指标

### 线程池配置

| 线程池 | 核心线程 | 最大线程 | 队列容量 | 拒绝策略 |
|--------|---------|---------|---------|---------|
| 通知处理 | 10 | 50 | 1000 | CallerRunsPolicy |
| 批量处理 | 5 | 20 | 500 | DiscardPolicy |

### 缓存策略

- 未读数量缓存时间: 1小时
- 缓存键: `notification:unread:{userId}`
- 操作后自动失效

### 数据库优化

- 关键字段都有索引
- 支持分页查询
- 支持按类型筛选

## 🔧 API 接口

### 获取通知列表
```
GET /notification/list?userId=1&pageNum=1&pageSize=10&type=user&isRead=0
```

### 获取未读数量
```
GET /notification/unread/count?userId=1
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

### 删除通知
```
DELETE /notification/{id}?userId=1
```

详见 `API接口文档.md` 第三部分

## 🎯 支持的通知类型

| 类型 | 动作 | 说明 |
|------|------|------|
| user | like_article | 文章被点赞 |
| user | like_comment | 评论被点赞 |
| user | comment_article | 文章被评论 |
| user | reply_comment | 评论被回复 |
| user | follow | 新增粉丝 |
| user | collect_article | 文章被收藏 |
| user | share_article | 文章被分享 |
| user | mention | @提及 |
| user | new_article | 新文章推送 |
| system | system_notice | 系统通知 |

## 🛡️ 并发控制

### 1. 异步处理
- 所有通知操作都是异步的
- 使用 @Async 注解
- 不阻塞主业务流程

### 2. 事务管理
- 每个操作都使用 @Transactional
- 异常时自动回滚
- 确保数据一致性

### 3. 错误处理
- 异步处理中的异常被捕获
- 所有异常都被记录
- 不影响主业务

### 4. 限流保护
- 线程池队列容量限制
- 拒绝策略防止资源耗尽
- 监控告警

## 📈 扩展建议

### 1. 消息队列集成
```java
// 使用 RabbitMQ 或 Kafka
rabbitTemplate.convertAndSend("notification.exchange", "notification.key", event);
```

### 2. WebSocket 推送
```java
// 实时推送给前端
webSocketService.sendNotification(userId, notification);
```

### 3. 邮件/短信通知
```java
// 重要通知发送邮件或短信
if ("important".equals(notification.getNotifyLevel())) {
    emailService.send(userId, notification);
}
```

### 4. 通知聚合
```java
// 使用 groupId 进行聚合
// 同一 groupId 的通知可以合并显示
```

## 🐛 故障排查

### 问题1: 通知没有被保存
- 检查 @EnableAsync 是否启用
- 检查 NotificationEventListener 是否被注入
- 查看日志中的异常

### 问题2: 缓存不一致
- 检查 Redis 连接
- 手动清除缓存
- 检查缓存失效逻辑

### 问题3: 线程池队列满
- 增加线程池大小
- 检查是否有长时间运行的任务
- 增加队列容量

### 问题4: 数据库性能下降
- 检查索引是否完整
- 检查批量插入的批次大小
- 考虑分表分库

## 📚 相关文档

- [通知系统实现指南](.kiro/steering/notification-system.md)
- [API接口文档](API接口文档.md)
- [数据库表结构](notification_table.sql)

## 🎓 最佳实践

1. **何时使用异步通知**
   - ✅ 点赞、评论、关注等用户互动
   - ✅ 新文章推送给粉丝
   - ✅ 审核结果通知
   - ❌ 不要用于需要立即反馈的操作

2. **何时使用批量通知**
   - ✅ 新文章推送给所有粉丝
   - ✅ 系统通知所有用户
   - ❌ 不要用于单个用户的通知

3. **缓存策略**
   - 未读数量频繁查询，使用缓存
   - 操作后立即失效缓存
   - 定期检查缓存是否过期

4. **错误处理**
   - 异步处理中的异常不会影响主业务
   - 所有异常都被记录，便于排查
   - 定期检查日志中的错误

## 📞 技术支持

如有问题，请查看：
1. 日志文件中的错误信息
2. 通知系统实现指南
3. 集成示例代码

## 📝 更新日志

### v1.0.0 (2024-03-11)
- ✅ 实现基础通知系统
- ✅ 集成点赞、关注、评论通知
- ✅ 支持异步处理和缓存
- ✅ 支持批量通知
- ✅ 完整的API接口
- ✅ 详细的文档和示例

## 📄 许可证

MIT License
