# 通知系统快速参考

## 🚀 快速开始 (5分钟)

### 1. 初始化数据库
```bash
mysql -u root -p < notification_table.sql
```

### 2. 启用异步处理
```java
@SpringBootApplication
@EnableAsync  // 添加这一行
public class Website1ModuleWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(Website1ModuleWebApplication.class, args);
    }
}
```

### 3. 启动应用
```bash
mvn spring-boot:run
```

## 📝 常用代码片段

### 发送点赞通知
```java
notificationPublisher.publishArticleLikeNotification(
    authorId, likerId, articleId, articleTitle
);
```

### 发送关注通知
```java
notificationPublisher.publishFollowNotification(
    followedUserId, followerId
);
```

### 发送评论通知
```java
notificationPublisher.publishArticleCommentNotification(
    authorId, commenterId, articleId, articleTitle, commentId, commentContent
);
```

### 发送回复通知
```java
notificationPublisher.publishCommentReplyNotification(
    commentAuthorId, replierId, parentCommentId, replyCommentId, replyContent
);
```

### 批量推送新文章
```java
batchNotificationService.notifyFollowersNewArticle(
    authorId, followerIds, articleId, articleTitle
);
```

### 系统通知所有用户
```java
batchNotificationService.notifyAllUsers(
    userIds, title, content, "important"
);
```

## 🔌 集成模板

### 在业务服务中集成

```java
@Service
@RequiredArgsConstructor
public class YourService {
    
    private final NotificationPublisher notificationPublisher;
    
    public void yourBusinessMethod() {
        // ... 业务逻辑
        
        // 发送通知
        try {
            notificationPublisher.publishXxxNotification(...);
        } catch (Exception e) {
            log.error("发送通知失败", e);
        }
    }
}
```

## 📊 API 速查表

| 操作 | 方法 | 路径 |
|------|------|------|
| 获取列表 | GET | `/notification/list` |
| 未读数量 | GET | `/notification/unread/count` |
| 类型统计 | GET | `/notification/unread/count/by-type` |
| 标记已读 | PUT | `/notification/{id}/read` |
| 批量已读 | PUT | `/notification/batch/read` |
| 全部已读 | PUT | `/notification/all/read` |
| 删除 | DELETE | `/notification/{id}` |
| 批量删除 | DELETE | `/notification/batch` |
| 清空已读 | DELETE | `/notification/clear/read` |
| 详情 | GET | `/notification/{id}` |

## 🧪 快速测试

### 测试点赞通知
```bash
# 1. 点赞
curl -X POST http://localhost:8080/like/article \
  -H "Content-Type: application/json" \
  -d '{"userId": 2, "articleId": 1, "actionLike": 1}'

# 2. 查看通知
curl http://localhost:8080/notification/list?userId=1
```

### 测试关注通知
```bash
# 1. 关注
curl -X POST http://localhost:8080/follow/add \
  -H "Content-Type: application/json" \
  -d '{"followerId": 2, "followingId": 1, "actionFollow": 1}'

# 2. 查看通知
curl http://localhost:8080/notification/list?userId=1
```

## 🔍 调试技巧

### 查看日志
```bash
tail -f logs/application.log | grep -i notification
```

### 检查Redis缓存
```bash
redis-cli
GET notification:unread:1
GET notification:unread:type:1
```

### 查看数据库
```sql
SELECT * FROM notification WHERE user_id = 1 ORDER BY created_at DESC;
```

## ⚙️ 配置调整

### 增加线程池大小
```java
// AsyncConfig.java
executor.setCorePoolSize(20);      // 改为20
executor.setMaxPoolSize(100);      // 改为100
executor.setQueueCapacity(5000);   // 改为5000
```

### 调整缓存时间
```java
// NotificationService.java
private static final long CACHE_EXPIRE_TIME = 1800;  // 改为30分钟
```

### 调整批量大小
```java
// BatchNotificationService.java
private static final int BATCH_SIZE = 1000;  // 改为1000
```

## 🐛 常见问题速解

| 问题 | 原因 | 解决 |
|------|------|------|
| 通知没保存 | @EnableAsync未启用 | 添加@EnableAsync |
| 缓存不一致 | Redis连接失败 | 检查Redis配置 |
| 队列满 | 线程池太小 | 增加线程池大小 |
| 性能下降 | 缺少索引 | 检查数据库索引 |

## 📚 文档导航

| 文档 | 用途 |
|------|------|
| NOTIFICATION_SYSTEM_README.md | 完整系统文档 |
| NOTIFICATION_INTEGRATION_GUIDE.md | 集成步骤 |
| .kiro/steering/notification-system.md | 详细实现 |
| IMPLEMENTATION_SUMMARY.md | 实现总结 |
| API接口文档.md | API参考 |

## 💡 最佳实践

✅ **DO**
- 使用异步通知，不阻塞主业务
- 使用批量服务处理大量通知
- 捕获异常，记录日志
- 定期检查日志和缓存

❌ **DON'T**
- 在同步方法中发送通知
- 一次性插入大量数据
- 忽略异常
- 频繁查询数据库

## 🎯 集成检查清单

- [ ] 数据库表已创建
- [ ] @EnableAsync已添加
- [ ] Redis已配置
- [ ] AsyncConfig已加载
- [ ] NotificationPublisher已注入
- [ ] 通知已发送
- [ ] API已测试
- [ ] 日志已验证
- [ ] 缓存已验证

## 📞 快速支持

### 问题排查步骤
1. 查看日志: `tail -f logs/application.log`
2. 检查Redis: `redis-cli`
3. 检查数据库: `SELECT * FROM notification`
4. 查看文档: 相关md文件

### 获取帮助
- 查看 NOTIFICATION_INTEGRATION_GUIDE.md
- 查看 .kiro/steering/notification-system.md
- 查看 NotificationIntegrationExample.java

---

**快速参考卡片** | 版本 1.0.0 | 2024-03-11
