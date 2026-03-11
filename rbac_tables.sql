-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(100) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(500) NULL COMMENT '描述',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` VARCHAR(1) DEFAULT '1' COMMENT '状态 0:禁用 1:启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT AUTO_INCREMENT COMMENT '权限ID',
    `parent_id` BIGINT DEFAULT 0 NULL COMMENT '父权限ID',
    `name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `type` VARCHAR(20) NOT NULL COMMENT '权限类型 MENU:菜单 BUTTON:按钮 API:接口',
    `path` VARCHAR(200) NULL COMMENT '前端路由路径',
    `component` VARCHAR(200) NULL COMMENT '前端组件路径',
    `icon` VARCHAR(100) NULL COMMENT '菜单图标',
    `method` VARCHAR(20) NULL COMMENT '请求方法 GET POST PUT DELETE',
    `api_path` VARCHAR(200) NULL COMMENT '接口路径',
    `sort` INT DEFAULT 0 NULL COMMENT '排序',
    `status` VARCHAR(1) DEFAULT '1' NULL COMMENT '状态 0:禁用 1:启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 插入示例数据
-- 角色数据
INSERT INTO `role` (`role_name`, `role_code`, `description`, `sort`, `status`) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 1, '1'),
('管理员', 'ADMIN', '拥有大部分管理权限', 2, '1'),
('版主', 'MODERATOR', '内容审核和管理', 3, '1'),
('普通用户', 'USER', '普通用户权限', 4, '1');

-- 权限数据（菜单）
INSERT INTO `permission` (`parent_id`, `name`, `permission_code`, `type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
(0, '用户管理', 'user_manage', 'MENU', '/user', 'UserManage', 'user', 1, '1'),
(0, '内容管理', 'content_manage', 'MENU', '/content', 'ContentManage', 'file-text', 2, '1'),
(0, '系统管理', 'system_manage', 'MENU', '/system', 'SystemManage', 'setting', 3, '1');

-- 权限数据（按钮/接口）
INSERT INTO `permission` (`parent_id`, `name`, `permission_code`, `type`, `method`, `api_path`, `sort`, `status`) VALUES
(1, '查看用户', 'user:view', 'API', 'GET', '/admin/user/list', 1, '1'),
(1, '创建用户', 'user:create', 'API', 'POST', '/admin/user', 2, '1'),
(1, '编辑用户', 'user:edit', 'API', 'PUT', '/admin/user/*', 3, '1'),
(1, '删除用户', 'user:delete', 'API', 'DELETE', '/admin/user/*', 4, '1'),
(2, '查看文章', 'article:view', 'API', 'GET', '/admin/article/list', 1, '1'),
(2, '审核文章', 'article:audit', 'API', 'POST', '/admin/article/audit/*', 2, '1'),
(2, '删除文章', 'article:delete', 'API', 'DELETE', '/admin/article/*', 3, '1');

-- 给超级管理员分配所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `permission`;
