-- 管理员表
CREATE TABLE IF NOT EXISTS `admin` (
    `id` BIGINT AUTO_INCREMENT COMMENT '管理员ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) NULL COMMENT '真实姓名',
    `email` VARCHAR(100) NULL COMMENT '邮箱',
    `phone` VARCHAR(20) NULL COMMENT '手机号',
    `avatar` VARCHAR(500) NULL COMMENT '头像',
    `status` VARCHAR(1) DEFAULT '1' COMMENT '状态 0:禁用 1:启用',
    `permissions` TEXT NULL COMMENT '权限（JSON格式）',
    `last_login_time` DATETIME NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) NULL COMMENT '最后登录IP',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 插入默认管理员（密码：admin123）
INSERT INTO `admin` (`username`, `password`, `real_name`, `email`, `status`) VALUES
('admin', 'admin123', '超级管理员', 'admin@example.com', '1');
