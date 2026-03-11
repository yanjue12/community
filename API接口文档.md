# 管理系统API接口文档

## 一、用户管理模块 (/admin/user)

### 1.1 用户CRUD

#### 1.1.1 获取用户列表（分页）
```
GET /admin/user/list
参数：
  - pageNum: 页码（默认1）
  - pageSize: 每页数量（默认10）
  - keyword: 搜索关键词（用户名/昵称/邮箱）
  - status: 用户状态
  - roleId: 按角色筛选
```

#### 1.1.2 获取用户详情
```
GET /admin/user/{id}
返回：用户信息 + 角色列表
```

#### 1.1.3 创建用户
```
POST /admin/user
Body: {
  "username": "用户名",
  "password": "密码",
  "nickname": "昵称",
  "email": "邮箱",
  "phone": "手机号",
  "avatar": "头像URL",
  "status": "1",
  "roleIds": [1, 2]  // 可选，同时分配角色
}
```

#### 1.1.4 更新用户（包含角色授权）
```
PUT /admin/user/{id}
Body: {
  "nickname": "新昵称",
  "email": "新邮箱",
  "phone": "新手机号",
  "avatar": "新头像",
  "status": "1",
  "roleIds": [1, 2, 3]  // 可选，更新用户角色
}
```

#### 1.1.5 删除用户
```
DELETE /admin/user/{id}
```

#### 1.1.6 批量删除用户
```
DELETE /admin/user/batch
Body: [1, 2, 3]
```

#### 1.1.7 重置用户密码
```
PUT /admin/user/{id}/reset-password
参数：
  - newPassword: 新密码
```

---

### 1.2 角色管理

#### 1.2.1 获取角色列表（分页）
```
GET /admin/user/role/list
参数：
  - pageNum: 页码
  - pageSize: 每页数量
  - keyword: 搜索关键词
```

#### 1.2.2 获取所有角色（不分页）
```
GET /admin/user/role/all
```

#### 1.2.3 获取角色详情
```
GET /admin/user/role/{id}
返回：角色信息 + 权限列表
```

#### 1.2.4 创建角色
```
POST /admin/user/role
Body: {
  "roleName": "角色名称",
  "roleCode": "ROLE_CODE",
  "description": "角色描述",
  "sort": 0,
  "status": "1",
  "permissionIds": [1, 2, 3]  // 可选，同时分配权限
}
```

#### 1.2.5 更新角色（包含权限分配）
```
PUT /admin/user/role/{id}
Body: {
  "roleName": "新角色名称",
  "roleCode": "NEW_CODE",
  "description": "新描述",
  "sort": 1,
  "status": "1",
  "permissionIds": [1, 2, 3, 4]  // 可选，更新角色权限
}
```

#### 1.2.6 删除角色
```
DELETE /admin/user/role/{id}
注意：如果有用户使用该角色，无法删除
```

---

### 1.3 权限管理

#### 1.3.1 获取权限列表（分页）
```
GET /admin/user/permission/list
参数：
  - pageNum: 页码
  - pageSize: 每页数量
  - keyword: 搜索关键词
```

#### 1.3.2 获取权限树（不分页）
```
GET /admin/user/permission/tree
```

#### 1.3.3 获取权限详情
```
GET /admin/user/permission/{id}
```

#### 1.3.4 创建权限
```
POST /admin/user/permission
Body: {
  "parentId": 0,
  "name": "权限名称",
  "permissionCode": "permission:code",
  "type": "MENU",  // MENU/BUTTON/API
  "path": "/path",
  "component": "Component",
  "icon": "icon-name",
  "method": "GET",
  "apiPath": "/api/path",
  "sort": 0,
  "status": "1"
}
```

#### 1.3.5 更新权限
```
PUT /admin/user/permission/{id}
Body: 同创建
```

#### 1.3.6 删除权限
```
DELETE /admin/user/permission/{id}
注意：如果有角色使用该权限或有子权限，无法删除
```

---

## 二、文章管理模块 (/admin/article)

### 2.1 文章列表
```
GET /admin/article/list
参数：
  - pageNum: 页码
  - pageSize: 每页数量
  - keyword: 标题关键词
  - status: 审核状态
```

### 2.2 多维度搜索文章
```
GET /admin/article/search
参数：
  - pageNum: 页码
  - pageSize: 每页数量
  - title: 文章标题
  - authorId: 作者ID
  - authorName: 作者名称
  - status: 审核状态
  - isTop: 是否置顶
  - isRecommend: 是否推荐
  - categoryId: 分类ID
  - startTime: 开始时间
  - endTime: 结束时间
  - minViews: 最小浏览量
  - maxViews: 最大浏览量
  - minLikes: 最小点赞量
  - maxLikes: 最大点赞量
```

### 2.3 文章详情
```
GET /admin/article/query/{id}
```

### 2.4 删除文章
```
DELETE /admin/article/{id}
```

### 2.5 批量删除
```
DELETE /admin/article/batch
Body: [1, 2, 3]
```

### 2.6 更新文章状态
```
PUT /admin/article/update/status
参数：
  - id: 文章ID
  - status: 状态值
```

### 2.7 置顶文章
```
PUT /admin/article/{id}/top
参数：
  - isTop: "1"或"0"
```

### 2.8 推荐文章
```
PUT /admin/article/{id}/recommend
参数：
  - isRecommend: "1"或"0"
```

### 2.9 审核通过
```
POST /admin/article/audit/manual/pass
参数：
  - articleId: 文章ID
  - adminId: 管理员ID
```

### 2.10 审核拒绝
```
POST /admin/article/audit/manual/reject
参数：
  - articleId: 文章ID
  - adminId: 管理员ID
  - reason: 拒绝原因
```

---

## 三、通知系统模块 (/notification)

### 3.1 获取通知列表
```
GET /notification/list
参数：
  - pageNum: 页码
  - pageSize: 每页数量
  - type: 通知类型（user/system/message）
  - isRead: 是否已读（0/1）
```

### 3.2 获取未读数量
```
GET /notification/unread/count
```

### 3.3 获取各类型未读数量
```
GET /notification/unread/count/by-type
返回：{
  "user": 10,
  "system": 2,
  "message": 5,
  "total": 17
}
```

### 3.4 标记单个为已读
```
PUT /notification/{id}/read
```

### 3.5 批量标记为已读
```
PUT /notification/batch/read
Body: [1, 2, 3]
```

### 3.6 全部标记为已读
```
PUT /notification/all/read
参数：
  - type: 可选，按类型标记
```

### 3.7 删除通知
```
DELETE /notification/{id}
```

### 3.8 批量删除
```
DELETE /notification/batch
Body: [1, 2, 3]
```

### 3.9 清空已读通知
```
DELETE /notification/clear/read
```

### 3.10 获取通知详情
```
GET /notification/{id}
注意：会自动标记为已读
```

---

## 四、通知触发场景

### 4.1 用户互动通知
- **点赞文章**: `notifyArticleLike(authorId, likerId, articleId, articleTitle)`
- **点赞评论**: `notifyCommentLike(commentAuthorId, likerId, commentId, commentContent)`
- **评论文章**: `notifyArticleComment(authorId, commenterId, articleId, articleTitle, commentId, commentContent)`
- **回复评论**: `notifyCommentReply(commentAuthorId, replierId, parentCommentId, replyCommentId, replyContent)`
- **关注用户**: `notifyFollow(followedUserId, followerId)`
- **收藏文章**: `notifyArticleCollect(authorId, collectorId, articleId, articleTitle)`
- **分享文章**: `notifyArticleShare(authorId, sharerId, articleId, articleTitle)`
- **@提及**: `notifyMention(mentionedUserId, mentionerId, contentType, contentId, content)`
- **新文章推送**: `notifyFollowerNewArticle(followerId, authorId, articleId, articleTitle)`

### 4.2 系统通知
- **系统消息**: `notifySystem(userId, title, content, level)`

---

## 五、数据字典

### 5.1 用户状态
- `0`: 禁用
- `1`: 正常
- `2`: 未激活

### 5.2 文章状态
- `0`: 草稿
- `1`: 已发布
- `2`: 审核拒绝
- `3`: 待审核
- `4`: 已删除

### 5.3 通知类型
- `user`: 用户互动
- `system`: 系统消息
- `message`: 私信提醒

### 5.4 通知动作类型
- `like_article`: 点赞文章
- `like_comment`: 点赞评论
- `comment_article`: 评论文章
- `reply_comment`: 回复评论
- `follow`: 关注
- `collect_article`: 收藏文章
- `share_article`: 分享文章
- `mention`: @提及
- `new_article`: 新文章
- `system_notice`: 系统通知

### 5.5 权限类型
- `MENU`: 菜单
- `BUTTON`: 按钮
- `API`: 接口

### 5.6 通知级别
- `normal`: 普通
- `important`: 重要
