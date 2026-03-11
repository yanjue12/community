# 通知系统快速集成指南

## 🎯 集成步骤

### 第1步: 数据库初始化

执行 `notification_table.sql` 创建通知表：

```bash
mysql -u root -p < notification_table.sql
```

### 第2步: 启用异步处理

在主应用类上添加 `@EnableAsync` 注解：

```java
@SpringBootApplication
@EnableAsync  // 添加这一行
public class Website1ModuleWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(Website1ModuleWebApplication.class, args);
    }
}
```

### 第3步: 验证配置

确保 `application.yml` 中有 Redis 配置：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
```

### 第4步: 启动应用

```bash
mvn spring-boot:run
```

## 📝 已集成的业务

以下业务已经集成了通知系统：

### ✅ 点赞通知 (LikeRecordServiceImpl)
- 文章被点赞时，自动发送通知给作者
- 异步处理，不阻塞点赞操作

### ✅ 关注通知 (FollowServiceImpl)
- 用户被关注时，自动发送通知
- 异步处理，不阻塞关注操作

### ✅ 评论通知 (CommentServiceImpl)
- 文章被评论时，发送通知给作者
- 评论被回复时，发送通知给评论者
- 异步处理，不阻塞评论操作

## 🔌 如何在其他业务中集成

### 示例1: 收藏文章通知

在 `FavoriteService` 中添加：

```java
@Service
@RequiredArgsConstructor
public class FavoriteService {
    
    private final NotificationPublisher notificationPublisher;
    
    public void collectArticle(Long userId, Long articleId) {
        // ... 保存收藏关系
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setArticleId(articleId);
        save(favorite);
        
        // 发送通知
        Article article = articleService.getById(articleId);
        if (article != null) {
            notificationPublisher.publishArticleCollectNotification(
                article.getAuthorId(), userId, articleId, article.getTitle()
            );
        }
    }
}
```

### 示例2: 分享文章通知

在 `ArticleService` 中添加：

```java
@Service
@RequiredArgsConstructor
public class ArticleService {
    
    private final NotificationPublisher notificationPublisher;
    
    public void shareArticle(Long userId, Long articleId) {
        // ... 记录分享
        Share share = new Share();
        share.setUserId(userId);
        share.setArticleId(articleId);
        shareMapper.insert(share);
        
        // 发送通知
        Article article = getById(articleId);
        if (article != null) {
            notificationPublisher.publishArticleShareNotification(
                article.getAuthorId(), userId, articleId, article.getTitle()
            );
        }
    }
}
```

### 示例3: 新文章推送给粉丝

在 `ArticleService` 中添加：

```java
@Service
@RequiredArgsConstructor
public class ArticleService {
    
    private final BatchNotificationService batchNotificationService;
    private final FollowService followService;
    
    public void publishArticle(Article article) {
        // ... 保存文章
        save(article);
        
        // 获取所有粉丝
        List<Long> followerIds = followService.getFollowerIds(article.getAuthorId());
        
        // 异步推送给所有粉丝
        if (!followerIds.isEmpty()) {
            batchNotificationService.notifyFollowersNewArticle(
                article.getAuthorId(), followerIds, article.getId(), article.getTitle()
            );
        }
    }
}
```

### 示例4: 审核通过/拒绝通知

在 `AuditService` 中添加：

```java
@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditNotificationService auditNotificationService;
    
    public void auditPass(Long articleId) {
        // ... 更新审核状态
        Article article = articleService.getById(articleId);
        auditNotificationService.notifyAuditPass(
            article.getAuthorId(), articleId, article.getTitle()
        );
    }
    
    public void auditReject(Long articleId, String reason) {
        // ... 更新审核状态
        Article article = articleService.getById(articleId);
        auditNotificationService.notifyAuditReject(
            article.getAuthorId(), articleId, article.getTitle(), reason
        );
    }
}
```

## 🧪 测试通知系统

### 测试1: 点赞通知

```bash
# 1. 用户A发布文章
POST /article/publish
{
  "title": "测试文章",
  "content": "测试内容",
  "authorId": 1
}

# 2. 用户B点赞文章
POST /like/article
{
  "userId": 2,
  "articleId": 1,
  "actionLike": 1
}

# 3. 查看用户A的通知
GET /notification/list?userId=1
```

### 测试2: 关注通知

```bash
# 1. 用户B关注用户A
POST /follow/add
{
  "followerId": 2,
  "followingId": 1,
  "actionFollow": 1
}

# 2. 查看用户A的通知
GET /notification/list?userId=1
```

### 测试3: 评论通知

```bash
# 1. 用户B评论用户A的文章
POST /comment/save
{
  "userId": 2,
  "articleId": 1,
  "content": "很好的文章",
  "authorId": 1
}

# 2. 查看用户A的通知
GET /notification/list?userId=1
```

## 📊 监控和调试

### 查看日志

```bash
# 查看通知相关的日志
tail -f logs/application.log | grep -i notification
```

### 检查未读数量

```bash
# 获取用户1的未读数量
curl http://localhost:8080/notification/unread/count?userId=1

# 获取各类型未读数量
curl http://localhost:8080/notification/unread/count/by-type?userId=1
```

### 检查Redis缓存

```bash
# 连接Redis
redis-cli

# 查看缓存的未读数量
GET notification:unread:1

# 查看缓存的类型统计
GET notification:unread:type:1
```

## 🚨 常见问题

### Q1: 通知没有被保存

**原因**: 
- @EnableAsync 没有启用
- NotificationEventListener 没有被注入
- 异步线程池没有配置

**解决**:
1. 检查主应用类是否有 @EnableAsync
2. 检查 AsyncConfig 是否被加载
3. 查看日志中的异常

### Q2: 缓存不一致

**原因**:
- Redis 连接失败
- 缓存失效逻辑有问题

**解决**:
1. 检查 Redis 连接
2. 手动清除缓存: `redis-cli DEL notification:unread:*`
3. 重新加载数据

### Q3: 线程池队列满

**原因**:
- 通知量太大
- 线程池配置太小

**解决**:
1. 增加线程池大小
2. 检查是否有长时间运行的任务
3. 增加队列容量

### Q4: 数据库性能下降

**原因**:
- 缺少索引
- 批量插入太大

**解决**:
1. 检查索引是否完整
2. 减小批量插入的批次大小
3. 考虑分表分库

## 📈 性能优化建议

### 1. 调整线程池大小

根据实际并发量调整 `AsyncConfig` 中的线程池配置：

```java
// 高并发场景
executor.setCorePoolSize(20);
executor.setMaxPoolSize(100);
executor.setQueueCapacity(5000);
```

### 2. 调整缓存时间

根据实际情况调整缓存过期时间：

```java
// 缓存时间改为30分钟
private static final long CACHE_EXPIRE_TIME = 1800;
```

### 3. 调整批量大小

根据数据库性能调整批量插入的大小：

```java
// 改为1000条一批
private static final int BATCH_SIZE = 1000;
```

### 4. 定期清理数据

添加定时任务清理已删除的通知：

```java
@Scheduled(cron = "0 0 2 * * *")  // 每天凌晨2点
public void cleanDeletedNotifications() {
    // 删除30天前的已删除通知
    notificationService.deleteOldNotifications(30);
}
```

## 🔗 相关资源

- [通知系统实现指南](.kiro/steering/notification-system.md)
- [API接口文档](API接口文档.md)
- [完整README](NOTIFICATION_SYSTEM_README.md)
- [数据库表结构](notification_table.sql)

## ✅ 集成检查清单

- [ ] 数据库表已创建
- [ ] @EnableAsync 已添加到主应用类
- [ ] Redis 配置已验证
- [ ] AsyncConfig 已加载
- [ ] NotificationPublisher 已注入到业务服务
- [ ] 通知事件已发布
- [ ] 通知已保存到数据库
- [ ] API 接口已测试
- [ ] 日志已验证
- [ ] 缓存已验证

## 📞 技术支持

如有问题，请：
1. 查看日志文件
2. 查看通知系统实现指南
3. 查看集成示例代码
4. 检查数据库和Redis连接

---

**最后更新**: 2024-03-11
**版本**: 1.0.0
