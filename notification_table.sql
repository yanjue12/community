-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `from_user_id` BIGINT NULL COMMENT '发送用户ID（系统通知时为NULL）',
    `type` VARCHAR(20) NOT NULL COMMENT '类型 system:系统消息 user:用户互动 message:私信提醒',
    `action_type` VARCHAR(50) NOT NULL COMMENT '动作类型（like_article, like_comment, comment_article, reply_comment, follow, collect_article, share_article, mention, new_article, system_notice等）',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NULL COMMENT '内容',
    `target_type` VARCHAR(50) NULL COMMENT '目标类型（article, comment, user, message等）',
    `target_id` BIGINT NULL COMMENT '目标ID',
    `parent_id` BIGINT NULL COMMENT '父级ID（如回复的评论ID）',
    `source_id` BIGINT NULL COMMENT '来源对象ID',
    `source_type` VARCHAR(50) NULL COMMENT '来源对象类型',
    `group_id` VARCHAR(100) NULL COMMENT '通知聚合ID（用于合并同类通知）',
    `extra_data` TEXT NULL COMMENT '额外数据（JSON格式）',
    `is_read` VARCHAR(1) DEFAULT '0' COMMENT '是否已读 0:未读 1:已读',
    `is_deleted` VARCHAR(1) DEFAULT '0' COMMENT '是否删除 0:未删除 1:已删除',
    `notify_level` VARCHAR(20) DEFAULT 'normal' COMMENT '通知级别 normal:普通 important:重要',
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

-- 通知类型说明
-- type字段：
--   system: 系统消息（系统公告、维护通知等）
--   user: 用户互动（点赞、评论、关注等）
--   message: 私信提醒

-- action_type字段（具体动作）：
--   like_article: 点赞文章
--   like_comment: 点赞评论
--   comment_article: 评论文章
--   reply_comment: 回复评论
--   follow: 关注用户
--   collect_article: 收藏文章
--   share_article: 分享文章
--   mention: @提及
--   new_article: 关注的作者发布新文章
--   system_notice: 系统通知

-- 示例数据
INSERT INTO `notification` (`user_id`, `from_user_id`, `type`, `action_type`, `title`, `content`, `target_type`, `target_id`, `group_id`, `is_read`, `notify_level`) VALUES
(1, 2, 'user', 'like_article', '文章获得点赞', '赞了你的文章《Spring Boot实战》', 'article', 100, 'like_article_100', '0', 'normal'),
(1, 3, 'user', 'comment_article', '文章收到评论', '评论了你的文章《Spring Boot实战》', 'article', 100, 'comment_article_100', '0', 'normal'),
(1, 4, 'user', 'follow', '新增粉丝', '关注了你', 'user', 4, 'follow_4', '0', 'normal'),
(1, NULL, 'system', 'system_notice', '系统维护通知', '系统将于今晚22:00-23:00进行维护', NULL, NULL, NULL, '0', 'important');
