-- 举报表
CREATE TABLE report (
    id BIGINT AUTO_INCREMENT COMMENT '举报ID' PRIMARY KEY,
    reporter_id BIGINT NOT NULL COMMENT '举报人ID',
    target_type VARCHAR(20) NOT NULL COMMENT '举报目标类型：article-文章, comment-评论, user-用户',
    target_id BIGINT NOT NULL COMMENT '举报目标ID',
    target_user_id BIGINT NOT NULL COMMENT '被举报用户ID',
    reason_type VARCHAR(20) NOT NULL COMMENT '举报原因类型：spam-垃圾信息, inappropriate-不当内容, harassment-骚扰, copyright-版权, other-其他',
    reason_detail VARCHAR(500) NULL COMMENT '详细举报原因',
    evidence_urls TEXT NULL COMMENT '举报证据图片URLs，多个用逗号分隔',
    status VARCHAR(10) DEFAULT 'pending' NOT NULL COMMENT '处理状态：pending-待处理, processing-处理中, resolved-已处理, rejected-已驳回',
    admin_id BIGINT NULL COMMENT '处理管理员ID',
    admin_remark VARCHAR(500) NULL COMMENT '管理员处理备注',
    processed_at DATETIME NULL COMMENT '处理时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '举报时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_reporter_id (reporter_id),
    INDEX idx_target_type_id (target_type, target_id),
    INDEX idx_target_user_id (target_user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) COMMENT '举报表';

-- 举报原因配置表（可选，用于管理举报原因选项）
CREATE TABLE report_reason (
    id BIGINT AUTO_INCREMENT COMMENT '原因ID' PRIMARY KEY,
    code VARCHAR(20) NOT NULL COMMENT '原因代码',
    name VARCHAR(50) NOT NULL COMMENT '原因名称',
    description VARCHAR(200) NULL COMMENT '原因描述',
    target_types VARCHAR(100) NOT NULL COMMENT '适用的目标类型，多个用逗号分隔',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(10) DEFAULT 'active' COMMENT '状态：active-启用, inactive-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    UNIQUE KEY uk_code (code),
    INDEX idx_status (status),
    INDEX idx_sort (sort)
) COMMENT '举报原因配置表';

-- 插入默认举报原因
INSERT INTO report_reason (code, name, description, target_types, sort) VALUES
('spam', '垃圾信息', '发布垃圾广告、刷屏等信息', 'article,comment,user', 1),
('inappropriate', '不当内容', '发布违法违规、低俗色情等不当内容', 'article,comment,user', 2),
('harassment', '骚扰行为', '恶意骚扰、人身攻击等行为', 'comment,user', 3),
('copyright', '版权侵犯', '未经授权使用他人作品', 'article', 4),
('misinformation', '虚假信息', '发布虚假、误导性信息', 'article,comment', 5),
('off_topic', '内容无关', '发布与主题无关的内容', 'article,comment', 6),
('duplicate', '重复内容', '重复发布相同或相似内容', 'article', 7),
('other', '其他原因', '其他不符合社区规范的行为', 'article,comment,user', 99);