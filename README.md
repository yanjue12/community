# 技术社区系统后端

## 项目说明

本项目是一个面向技术内容分享、互动交流、在线题库与后台管理的社区系统后端服务。系统提供文章发布、文章审核、分类标签、用户登录注册、点赞收藏、评论、关注、私信、通知、举报、推荐、搜索、题库试卷、错题本、AI 助手、管理后台统计等能力。

当前仓库主要是后端服务，采用 Maven 多模块结构组织代码。前端项目需要单独启动，并通过后端地址或前端代理访问接口。

## 项目技术栈

| 类型 | 技术 |
| --- | --- |
| 开发语言 | Java 8/11 |
| 后端框架 | Spring Boot 2.7.6 |
| 项目构建 | Maven |
| ORM | MyBatis-Plus 3.5.5、MyBatis XML |
| 数据库 | MySQL 8 |
| 连接池 | Druid |
| 缓存 | Redis |
| 权限认证 | Sa-Token |
| 搜索 | Elasticsearch 7.17.x |
| 文件存储 | MinIO |
| 实时通信 | WebSocket |
| 接口文档 | Springdoc OpenAPI |
| JSON 工具 | Fastjson、Jackson |
| 工具库 | Hutool、Jsoup |
| 导出能力 | Apache POI、OpenPDF |
| 邮件服务 | Spring Mail |
| 短信服务 | 阿里云短信 |
| AI 能力 | 火山方舟/本地 AI 平台配置 |

## 项目结构

```text
community
+-- pom.xml                         # 父工程，管理依赖和模块
+-- Dockerfile                      # 后端镜像构建文件
+-- website1-common                 # 公共模块
|   +-- website1-base               # 基础常量、枚举、Result、Redis Key 等
|   +-- website1-core               # 核心配置、异常处理、Redis/MyBatis/MinIO/ES/CORS/Sa-Token 配置
+-- website1-module-web             # Web 启动模块
    +-- src/main/java/com/fzg
    |   +-- controller              # 用户端和管理端接口
    |   +-- service                 # 业务接口
    |   +-- service/impl            # 业务实现
    |   +-- mapper                  # MyBatis Mapper
    |   +-- model                   # 数据库实体
    |   +-- vo                      # 请求和响应对象
    |   +-- task                    # 定时任务
    |   +-- websocket               # WebSocket 通知
    |   +-- essync                  # ES 同步
    +-- src/main/resources
        +-- application.yml         # 公共配置，默认激活 dev
        +-- application-dev.yml     # 开发环境配置
        +-- application-prod.yml    # 生产环境配置
        +-- mapper                  # MyBatis XML
```

## 需要启动的服务

本项目启动前建议先启动以下基础服务：

| 服务 | 默认地址 | 是否必需 | 说明 |
| --- | --- | --- | --- |
| MySQL | `localhost:3306` | 必需 | 业务数据存储，开发环境数据库名为 `community` |
| Redis | `127.0.0.1:6379` | 必需 | 登录状态、验证码、推荐缓存、曝光去重、消息状态等 |
| MinIO | `http://127.0.0.1:9000` | 必需 | 头像、封面图、文章图片等文件存储 |
| Elasticsearch | `http://localhost:9200` | 建议启动 | 文章搜索、索引同步 |
| SMTP 邮件服务 | `smtp.163.com:465` | 可选 | 邮箱验证码、找回密码、邮箱绑定 |
| 阿里云短信 | 环境变量配置 | 可选 | 手机验证码登录/验证 |
| 腾讯验证码 | 环境变量配置 | 可选 | 注册/登录图形验证码校验 |
| AI 平台 | `http://127.0.0.1:8000` 或火山方舟 | 可选 | AI 聊天、多模态向量等能力 |

## 服务配置

### 后端服务端口

开发环境配置文件：`website1-module-web/src/main/resources/application-dev.yml`

```yaml
server:
  port: 9322
  http-port: 9321
```

后端本地访问地址：

```text
http://localhost:9322
```

接口文档地址：

```text
http://localhost:9322/swagger-ui/index.html
```

如果前端使用 `/api` 作为代理前缀，需要在前端开发服务器中把 `/api` 转发到：

```text
http://localhost:9322
```

### MySQL 配置

开发环境默认配置：

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/community?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
      username: root
      password: <本地数据库密码>
      driver-class-name: com.mysql.cj.jdbc.Driver
```

启动前需要确认：

```text
1. MySQL 已启动
2. 已创建 community 数据库
3. 数据库账号密码和配置文件一致
4. 初始化 SQL 已执行
```

### Redis 配置

```yaml
spring:
  redis:
    database: 0
    host: 127.0.0.1
    port: 6379
```

Redis 主要用于：

```text
登录 token 状态
邮箱/短信验证码
点赞缓存
推荐结果缓存
推荐曝光去重
用户画像脏标记
WebSocket 在线状态
```

### MinIO 配置

```yaml
minio:
  endpoint: http://127.0.0.1:9000
  access-key: <MinIO AccessKey>
  secret-key: <MinIO SecretKey>
  bucket-name: website
  temp-bucket-name: temp-image
```

需要提前创建 bucket：

```text
website
temp-image
```

### Elasticsearch 配置

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
```

如果使用搜索功能，需要启动 ES，并通过管理接口同步文章索引：

```http
POST /admin/es/syncArticles
```

### Sa-Token 配置

```yaml
sa-token:
  token-name: Authorization
  timeout: 2592000
  activity-timeout: -1
  is-concurrent: true
  is-share: false
  token-style: uuid
```

请求登录后，前端需要在请求头中携带：

```http
Authorization: 登录返回的 token
```

### 邮件配置

```yaml
spring:
  mail:
    host: smtp.163.com
    port: 465
    username: <邮箱账号>
    password: 邮箱授权码
    protocol: smtp
```

### 短信配置

短信配置支持环境变量：

```text
ALIYUN_SMS_ENABLED
ALIYUN_SMS_REGION
ALIYUN_SMS_ENDPOINT
ALIYUN_SMS_COUNTRY_CODE
ALIYUN_SMS_SCHEME_NAME
ALIYUN_SMS_SIGN_NAME
ALIYUN_SMS_TEMPLATE_CODE
ALIYUN_SMS_VALID_TIME
ALIYUN_SMS_CODE_LENGTH
ALIYUN_SMS_INTERVAL
ALIYUN_SMS_RETURN_VERIFY_CODE
```

开发环境中 `ALIYUN_SMS_RETURN_VERIFY_CODE=true` 时，接口可以返回验证码，方便本地调试。

### AI 配置

```text
ARK_API_KEY
ARK_DEFAULT_DEPTH
AI_PLATFORM_ENABLED
AI_PLATFORM_BASE_URL
AI_PLATFORM_API_KEY
AI_PLATFORM_ALLOW_DEGRADE
```

默认模型配置在 `application.yml`：

```yaml
ark:
  ai:
    base-url: https://ark.cn-beijing.volces.com/api/v3
    model: doubao-seed-2-0-pro-260215
    embedding-model: doubao-embedding-vision-251215
```

## 启动方式

### 本地启动

```bash
mvn clean package -DskipTests
java -jar website1-module-web/target/website1-module-web-0.0.1-SNAPSHOT.jar
```

也可以在 IDE 中直接运行：

```text
com.fzg.Website1ModuleWebApplication
```

默认激活环境：

```yaml
spring:
  profiles:
    active: dev
```

### Docker 启动

构建镜像：

```bash
docker build -t community-backend .
```

运行容器：

```bash
docker run -d --name community-backend -p 9322:9322 community-backend
```

生产环境配置使用 `application-prod.yml`，其中 MySQL、Redis 使用容器服务名：

```text
mysql:3306
redis:6379
```

## 项目模块说明

### 1. 公共基础模块 `website1-common/website1-base`

功能：

```text
统一返回对象 Result
公共枚举
Redis Key 常量
推荐、验证码、关注、文章等缓存 Key 管理
短信工具类
```

主要内容：

| 类/包 | 功能 |
| --- | --- |
| `com.fzg.model.Result` | 统一接口返回结构 |
| `com.fzg.enums` | 行为类型、文章列表类型等枚举 |
| `com.fzg.constant` | Redis Key 常量 |
| `com.fzg.util.SMSUtils` | 短信工具 |

### 2. 公共核心模块 `website1-common/website1-core`

功能：

```text
跨域配置
Sa-Token 拦截配置
Redis 配置
MyBatis-Plus 配置
MinIO 配置
Elasticsearch 配置
全局异常处理
JSON 类型处理
```

主要内容：

| 类/包 | 功能 |
| --- | --- |
| `CorsConfig` | 允许前端跨域访问 |
| `SaTokenConfig` | 登录认证拦截 |
| `RedisConfig` | Redis 序列化配置 |
| `MybatisPlusConfig` | MyBatis-Plus 分页等配置 |
| `MinioConfig` | MinIO 客户端配置 |
| `ElasticsearchConfig` | ES 客户端配置 |
| `GlobalExceptionHandle` | 全局异常返回 |

### 3. 用户认证与个人中心模块

功能：

```text
用户注册
用户名/邮箱密码登录
手机号验证码登录
验证码发送与校验
个人资料查询和修改
密码修改和找回
邮箱绑定
隐私设置
用户兴趣标签初始化
GitHub OAuth 登录/绑定
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `POST` | `/auth/register` | 用户注册 |
| `POST` | `/auth/login` | 用户名/邮箱密码登录 |
| `POST` | `/auth/login-phone-code` | 手机号验证码登录 |
| `POST` | `/auth/logout` | 退出登录 |
| `POST` | `/auth/queryCurLoginUserInfo` | 查询当前登录用户 |
| `POST` | `/auth/queryUserInfo` | 查询用户信息 |
| `POST` | `/auth/editInfo` | 修改个人资料 |
| `POST` | `/auth/updatePassword` | 修改密码 |
| `POST` | `/auth/forgetPassword` | 找回密码 |
| `POST` | `/auth/send-code` | 发送邮箱验证码 |
| `POST` | `/auth/send-sms-code` | 发送短信验证码 |
| `POST` | `/auth/verify-sms-code` | 校验短信验证码 |
| `POST` | `/auth/initInterestTags` | 初始化兴趣标签 |
| `POST` | `/auth/updatePrivateSetting` | 修改隐私设置 |
| `POST` | `/auth/queryPrivateSetting` | 查询隐私设置 |
| `GET` | `/oauth/github/login` | GitHub 登录跳转 |
| `GET` | `/oauth/github/bind` | GitHub 绑定跳转 |
| `GET` | `/oauth/github/callback` | GitHub 回调 |

### 4. 文章内容模块

功能：

```text
文章发布
文章修改
文章详情
文章列表
文章搜索
推荐列表
待审核文章
文章撤回
文章删除
阅读时长记录
点赞
收藏
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `POST` | `/auth/publishArticle` | 发布文章 |
| `POST` | `/auth/updateArticle` | 修改文章 |
| `POST` | `/article/detail` | 查询文章详情 |
| `POST` | `/article/queryArticleById` | 根据 ID 查询文章 |
| `POST` | `/article/queryList` | 查询文章列表/推荐列表 |
| `POST` | `/article/search` | 搜索文章 |
| `POST` | `/article/search/suggestions` | 搜索建议 |
| `POST` | `/article/readTime` | 上报阅读时长 |
| `POST` | `/article/like` | 点赞/取消点赞 |
| `POST` | `/article/favorite` | 收藏/取消收藏 |
| `POST` | `/article/queryLikeArtById` | 查询用户点赞文章 |
| `POST` | `/article/queryFavArtById` | 查询用户收藏文章 |
| `POST` | `/article/queryPendingArticles` | 查询待审核文章 |
| `POST` | `/article/recallPendingArticles` | 撤回待审核文章 |
| `POST` | `/article/deleteArt` | 删除文章 |

### 5. 推荐与用户画像模块

功能：

```text
基于用户行为生成用户画像
用户协同过滤推荐
内容标签推荐
热门内容兜底
多路召回加权融合
曝光去重
推荐效果日志记录
```

实现要点：

```text
行为来源：浏览、点赞、收藏、评论、分享
画像维度：标签、分类、作者
时间范围：近 90 天行为
协同过滤：余弦相似度 + Top30 相似用户
融合权重：CF 50%、内容标签 35%、热门 15%
内容探索：内容召回中约 50% 为探索内容
缓存：Redis 推荐列表和曝光集合
```

相关接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `POST` | `/article/queryList` | 根据列表类型返回推荐/热门/分类等文章 |
| `POST` | `/auth/initInterestTags` | 初始化用户兴趣画像 |
| `POST` | `/article/readTime` | 记录阅读行为 |
| `POST` | `/article/like` | 记录点赞行为 |
| `POST` | `/article/favorite` | 记录收藏行为 |

定时任务：

| 任务 | 频率 | 功能 |
| --- | --- | --- |
| `UserProfileRefreshTask` | 每 60 秒 | 刷新有新行为的用户画像 |
| `ArticleViewHistoryCleanupTask` | 每天 02:00 | 清理历史浏览记录 |

### 6. 分类与标签模块

功能：

```text
文章分类查询
分类树管理
分类统计
标签管理
热门标签
引导标签
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `POST` | `/category/list` | 用户端分类列表 |
| `POST` | `/articleTag/queryHotTags` | 查询热门标签 |
| `POST` | `/articleTag/queryGuideTags` | 查询引导标签 |
| `POST` | `/admin/category/list` | 后台分类列表 |
| `GET` | `/admin/category/statistics` | 分类统计 |
| `POST` | `/admin/category/create` | 新增分类 |
| `DELETE` | `/admin/category/{id}` | 删除分类 |
| `DELETE` | `/admin/category/batch` | 批量删除分类 |
| `PUT` | `/admin/category/{id}/status` | 修改分类状态 |
| `GET` | `/admin/tag/statistics` | 标签统计 |
| `GET` | `/admin/tag/list` | 标签列表 |
| `GET` | `/admin/tag/{id}` | 标签详情 |
| `POST` | `/admin/tag/create` | 新增标签 |
| `PUT` | `/admin/tag/{id}` | 修改标签 |
| `DELETE` | `/admin/tag/{id}` | 删除标签 |
| `DELETE` | `/admin/tag/batch` | 批量删除标签 |
| `GET` | `/admin/tag/hot` | 热门标签 |

### 7. 评论、关注、收藏互动模块

功能：

```text
发表评论
回复评论
删除评论
分页查询根评论和子评论
关注/取消关注
粉丝列表
关注列表
关注数和粉丝数统计
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `POST` | `/comment/add` | 新增评论 |
| `GET` | `/comment/root/page` | 根评论分页 |
| `GET` | `/comment/child/page` | 子评论分页 |
| `POST` | `/comment/delete` | 删除评论 |
| `POST` | `/follow/add` | 关注/取消关注 |
| `POST` | `/follow/queryFolList` | 查询粉丝列表 |
| `POST` | `/follow/queryFollingList` | 查询关注列表 |
| `POST` | `/follow/getFolingAndFolCount` | 查询关注数和粉丝数 |

### 8. 草稿箱模块

功能：

```text
文章草稿保存
草稿查询
草稿删除
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `POST` | `/draft/saveArticleDraft` | 保存文章草稿 |
| `POST` | `/draft/queryArticleDraft` | 查询文章草稿 |
| `POST` | `/draft/deleteArticleDraft` | 删除文章草稿 |

### 9. 举报与审核模块

功能：

```text
用户举报文章/评论/用户
举报原因配置
举报记录查询
后台处理举报
文章审核
审核通过/拒绝
批量审核
审核历史
审核统计
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `POST` | `/report/submit` | 提交举报 |
| `GET` | `/report/reasons/all` | 查询举报原因 |
| `GET` | `/report/check` | 检查是否已举报 |
| `GET` | `/report/my-reports` | 我的举报记录 |
| `DELETE` | `/report/{reportId}` | 删除举报 |
| `POST` | `/admin/report/list` | 后台举报列表 |
| `POST` | `/admin/report/{reportId}/action` | 处理举报 |
| `POST` | `/admin/report/batch-process` | 批量处理举报 |
| `GET` | `/admin/report/statistics` | 举报统计 |
| `GET` | `/admin/report/recent` | 最近举报 |
| `POST` | `/admin/audit/list` | 审核列表 |
| `POST` | `/admin/audit/{auditId}/approve` | 审核通过 |
| `POST` | `/admin/audit/{auditId}/reject` | 审核拒绝 |
| `POST` | `/admin/audit/batch` | 批量审核 |
| `GET` | `/admin/audit/history` | 审核历史 |
| `GET` | `/admin/audit/statistics` | 审核统计 |
| `GET` | `/admin/audit/article/{articleId}` | 查询文章审核记录 |
| `GET` | `/admin/audit/recent` | 最近审核记录 |

### 10. 通知、私信与 WebSocket 模块

功能：

```text
站内通知
通知未读数
通知批量已读
按分类已读
通知删除
在线用户统计
私信会话
私信发送
私信已读
WebSocket 实时通知推送
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `GET` | `/notification/list` | 通知列表 |
| `GET` | `/notification/list/category` | 按分类查询通知 |
| `GET` | `/notification/unread-count` | 未读通知数 |
| `POST` | `/notification/mark-read-batch` | 批量标记已读 |
| `POST` | `/notification/mark-read-category` | 分类标记已读 |
| `DELETE` | `/notification/{notificationId}` | 删除通知 |
| `DELETE` | `/notification/all` | 清空通知 |
| `GET` | `/notification/online-users` | 在线用户列表 |
| `GET` | `/notification/online-count` | 在线用户数 |
| `GET` | `/notification/online-status/{userId}` | 用户在线状态 |
| `POST` | `/notification/system/broadcast` | 系统广播 |
| `GET` | `/message/conversations` | 私信会话列表 |
| `GET` | `/message/conversation/{targetUserId}` | 获取或创建会话 |
| `GET` | `/message/list/{conversationId}` | 私信消息列表 |
| `POST` | `/message/send` | 发送私信 |
| `POST` | `/message/read/{conversationId}` | 会话标记已读 |
| `GET` | `/message/unread` | 私信未读数 |

WebSocket 地址：

```text
ws://localhost:9322/ws/notification/{userId}?token={Authorization}
```

定时任务：

| 任务 | 频率 | 功能 |
| --- | --- | --- |
| `WebSocketSessionCleanupTask` | 每 5 分钟 | 清理失效连接 |
| `WebSocketSessionCleanupTask` | 每 1 小时 | 输出/维护连接统计 |
| `NotificationCleanupTask` | 每天/每周 | 清理过期通知 |
| `NotificationPartitionTask` | 每天 03:00 | 维护通知分区 |

### 11. 题库、试卷与错题本模块

功能：

```text
题目管理
题目标签
试卷管理
试卷组题
用户考试
答题暂存
交卷判分
考试结果
错题本
错题掌握状态
错题标签统计
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `GET` | `/exam/question/list` | 用户端题目列表 |
| `GET` | `/exam/paper/list` | 用户端试卷列表 |
| `GET` | `/exam/paper/{paperId}` | 试卷详情 |
| `POST` | `/exam/paper/{paperId}/start` | 开始考试 |
| `POST` | `/exam/attempt/{attemptId}/save` | 保存答题 |
| `POST` | `/exam/attempt/{attemptId}/submit` | 提交试卷 |
| `GET` | `/exam/attempt/{attemptId}/result` | 考试结果 |
| `POST` | `/wrong/list` | 错题列表 |
| `POST` | `/wrong/detail` | 错题详情 |
| `POST` | `/wrong/master` | 标记掌握 |
| `POST` | `/wrong/remove` | 移除错题 |
| `POST` | `/wrong/stats/tag` | 错题标签统计 |
| `POST` | `/admin/exam/question/list` | 后台题目列表 |
| `GET` | `/admin/exam/question/{id}` | 题目详情 |
| `GET` | `/admin/exam/question/tags` | 题目标签选项 |
| `POST` | `/admin/exam/question/create` | 新增题目 |
| `PUT` | `/admin/exam/question/update` | 修改题目 |
| `DELETE` | `/admin/exam/question/{id}` | 删除题目 |
| `POST` | `/admin/exam/paper/list` | 后台试卷列表 |
| `GET` | `/admin/exam/paper/{paperId}` | 试卷详情 |
| `POST` | `/admin/exam/paper/create` | 新增试卷 |
| `PUT` | `/admin/exam/paper/update` | 修改试卷 |
| `PUT` | `/admin/exam/paper/{paperId}/questions` | 修改试卷题目 |
| `DELETE` | `/admin/exam/paper/{paperId}` | 删除试卷 |

### 12. 文件上传模块

功能：

```text
本地文件上传到 MinIO
URL 图片转存
文件下载
文件删除
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `POST` | `/minio/upload` | 上传文件 |
| `POST` | `/minio/uploadByUrl` | 通过 URL 上传 |
| `GET` | `/minio/download/{objectName}` | 下载文件 |
| `DELETE` | `/minio/delete/{objectName}` | 删除文件 |

### 13. AI 助手模块

功能：

```text
AI 对话
流式对话
会话历史
会话详情
重置会话
多模态向量
绘图历史
模型列表
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `POST` | `/bot/chat` | AI 普通对话 |
| `POST` | `/bot/chat/stream` | AI 流式对话 |
| `GET` | `/bot/chat/history` | 对话历史 |
| `GET` | `/bot/chat/sessions` | 会话列表 |
| `GET` | `/bot/chat/session/detail` | 会话详情 |
| `POST` | `/bot/chat/reset` | 重置会话 |
| `POST` | `/bot/embedding/multimodal` | 多模态向量 |
| `GET` | `/bot/drawing/history` | 绘图历史 |
| `POST` | `/bot/drawing/history/delete` | 删除绘图历史 |
| `GET` | `/bot/models` | 可用模型列表 |

### 14. 后台用户、角色与权限模块

功能：

```text
后台用户列表
用户统计
用户创建/编辑
用户状态管理
角色管理
权限树管理
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `GET` | `/admin/user/statistics` | 用户统计 |
| `POST` | `/admin/user/list` | 用户列表 |
| `GET` | `/admin/user/{id}` | 用户详情 |
| `POST` | `/admin/user/create` | 新增用户 |
| `PUT` | `/admin/user/edit/{id}` | 编辑用户 |
| `PUT` | `/admin/user/{id}` | 修改用户 |
| `GET` | `/admin/user/check-email` | 检查邮箱 |
| `GET` | `/admin/user/roles` | 角色选项 |
| `GET` | `/admin/user/role/list` | 角色列表 |
| `POST` | `/admin/user/role` | 新增角色 |
| `PUT` | `/admin/user/role/{id}` | 修改角色 |
| `DELETE` | `/admin/user/role/{id}` | 删除角色 |
| `GET` | `/admin/user/permission/tree` | 权限树 |
| `GET` | `/admin/user/permission/{id}` | 权限详情 |
| `POST` | `/admin/user/permission` | 新增权限 |
| `PUT` | `/admin/user/permission/{id}` | 修改权限 |
| `DELETE` | `/admin/user/permission/{id}` | 删除权限 |

### 15. 后台文章、统计与系统配置模块

功能：

```text
文章后台管理
文章状态修改
置顶/推荐
文章导出
管理端仪表盘
用户增长趋势
文章发布趋势
活跃用户
实时动态
待办事项
统计报表导出
系统配置管理
配置分组管理
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `GET` | `/admin/article/statistics` | 文章统计 |
| `POST` | `/admin/article/list` | 后台文章列表 |
| `POST` | `/admin/article/query/{id}` | 文章详情 |
| `DELETE` | `/admin/article/delete/{id}` | 删除文章 |
| `DELETE` | `/admin/article/batch` | 批量删除文章 |
| `PUT` | `/admin/article/update/status/{id}` | 修改文章状态 |
| `PUT` | `/admin/article/{id}/top` | 置顶/取消置顶 |
| `PUT` | `/admin/article/{id}/recommend` | 推荐/取消推荐 |
| `GET` | `/admin/article/export` | 导出文章 |
| `GET` | `/admin/analytics/dashboard` | 仪表盘数据 |
| `GET` | `/admin/analytics/user-growth-trend` | 用户增长趋势 |
| `GET` | `/admin/analytics/article-publish-trend-advanced` | 文章发布趋势 |
| `GET` | `/admin/analytics/active-users` | 活跃用户 |
| `GET` | `/admin/analytics/user-growth-comparison` | 用户增长对比 |
| `GET` | `/admin/analytics/realtime-activities` | 实时动态 |
| `POST` | `/admin/analytics/pending-tasks` | 待办事项 |
| `GET` | `/admin/analytics/export/excel` | 导出 Excel |
| `GET` | `/admin/analytics/export/pdf` | 导出 PDF |
| `GET` | `/admin/system/config/list` | 系统配置列表 |
| `GET` | `/admin/system/config/{id}` | 系统配置详情 |
| `POST` | `/admin/system/config` | 新增系统配置 |
| `PUT` | `/admin/system/config/{id}` | 修改系统配置 |
| `DELETE` | `/admin/system/config/{id}` | 删除系统配置 |
| `GET` | `/admin/system/config/groups` | 配置分组列表 |
| `POST` | `/admin/system/config/group` | 新增配置分组 |
| `PUT` | `/admin/system/config/group/{id}` | 修改配置分组 |
| `DELETE` | `/admin/system/config/group/{id}` | 删除配置分组 |

### 16. 搜索与 ES 同步模块

功能：

```text
文章搜索
搜索建议
热门搜索词
MySQL 文章数据同步到 Elasticsearch
```

主要接口：

| 方法 | 接口 | 功能 |
| --- | --- | --- |
| `POST` | `/article/search` | 文章搜索 |
| `POST` | `/article/search/suggestions` | 搜索建议 |
| `POST` | `/searchHot` | 热搜词 |
| `POST` | `/admin/es/syncArticles` | 全量同步文章到 ES |

## 定时任务汇总

| 任务类 | 触发时间 | 作用 |
| --- | --- | --- |
| `UserProfileRefreshTask` | 每 60 秒 | 刷新用户画像并清理推荐缓存 |
| `ArticleViewHistoryCleanupTask` | 每天 02:00 | 清理文章浏览历史 |
| `NotificationPartitionTask` | 每天 03:00 | 创建/维护通知分区 |
| `NotificationCleanupTask` | 每天 03:00、04:00、05:00 和每周 | 清理旧通知、分区和统计数据 |
| `SearchHistoryCleanupTask` | 每天 04:00 | 清理搜索历史 |
| `WebSocketSessionCleanupTask` | 每 5 分钟/每 1 小时 | 清理 WebSocket 会话和输出统计 |

## 常用测试账号

以下账号仅用于本地演示和测试环境，生产环境请删除或修改默认密码。

```text
admin: 123456
coder_li: 123456
test_user1: 123456
test: fzg3342091
```

## 访问地址

```text
后端本地地址：http://localhost:9322
前端开发地址：http://localhost:5173
线上域名：https://datawisdom.tech
用户端路径：/app/
管理端路径：/admin/
```

## 常见问题

### 1. 接口 401 或提示未登录

登录后需要在请求头携带：

```http
Authorization: token
```

### 2. 图片上传失败

检查 MinIO：

```text
1. MinIO 服务是否启动
2. endpoint、access-key、secret-key 是否正确
3. bucket 是否已创建
4. 前端访问图片时 MinIO 地址是否能被浏览器访问
```

### 3. 搜索没有结果

检查：

```text
1. Elasticsearch 是否启动
2. spring.elasticsearch.uris 是否正确
3. 是否调用过 POST /admin/es/syncArticles 同步文章索引
```

### 4. 手机验证码无法发送

检查：

```text
1. ALIYUN_SMS_ENABLED 是否开启
2. 短信签名和模板是否配置
3. 本地调试可使用 ALIYUN_SMS_RETURN_VERIFY_CODE=true
```

### 5. 推荐结果重复

推荐模块依赖 Redis 记录曝光集合。如果 Redis 被清空，短时间内可能出现重复推荐；用户继续浏览后会重新建立曝光记录。
