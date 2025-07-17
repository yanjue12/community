
create database if not exists kouzi;
use kouzi;

drop table if exists user;
drop table if exists admin;
drop table if exists news_detail;
drop table if exists news;
drop table if exists news_type;
drop table if exists subtitles;
drop table if exists solutions;

drop table if exists system_config;
drop table if exists operation_log;



-- 创建用户表
CREATE TABLE if not exists user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,    -- 用户ID
    account varchar(255) unique comment '用户账号',
    username VARCHAR(255) NOT NULL,              -- 用户名称
    password varchar(255) not null comment '用户密码',
    sex tinyint comment '0-男 1-女',
    email VARCHAR(255) NOT NULL UNIQUE,      -- 用户邮箱
    phone VARCHAR(20),                       -- 用户电话
    url varchar(255) default null comment '头像url',
    states tinyint default 1 comment '1-正常 2-异常',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    role varchar(20) default 'user' comment '角色：user-普通用户 admin-管理员'
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin comment '用户表';

 -- 管理员表
create table if not exists admin(
    id int auto_increment primary key comment 'id',
    account varchar(255) unique comment '账号',
    role varchar(20) comment '角色：管理员',
    username varchar(255) not null comment '用户名',
    password varchar(255) not null comment '密码',
    email varchar(255) not null unique comment '邮箱'
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin comment '管理员表';


 -- 创建新闻类型表
/*CREATE TABLE if not exists news_type (
    id tinyint PRIMARY KEY AUTO_INCREMENT,        -- 新闻类型ID
    name VARCHAR(100) NOT NULL comment '新闻类型：如：Product ，Event，Partnership',                 -- 新闻类型名称（如：Product, Event, Partnership, Corporate）
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 更新时间
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '新闻类型表';
*/
-- 企业新闻表
CREATE TABLE if not exists news (
    id int PRIMARY KEY AUTO_INCREMENT,        -- 新闻ID
    title VARCHAR(255) NOT NULL comment '新闻标题',                 -- 新闻标题
    summary varchar(255) comment '简介',
    label String not null comment '新闻类型',
    states tinyint default 1 comment '状态：1-正常 0-下架',
    url varchar(255) null comment '封面图片url',
    publish_date datetime DEFAULT current_timestamp comment '发布时间 默认为创建当天',                  -- 发布时间
    author varchar(50) not null comment '发布人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin  COMMENT '新闻表';


-- 新闻详情
create table if not exists news_detail(
    id int primary key auto_increment comment 'id',
    news_id int comment '新闻id',
    content text not null comment '存储HTML内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    UNIQUE KEY uniq_news_id (news_id), -- 确保一对一
    foreign key (news_id) references news(id) on delete cascade
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin comment '新闻详情表';

-- solutions(解决方案)
 CREATE TABLE if not exists solutions (
    id int PRIMARY KEY AUTO_INCREMENT,          -- 主键
    image_url VARCHAR(255) not null comment '图片url',                         -- 图片存储路径/URL
    title VARCHAR(100) NOT NULL comment '主标题',                   -- 主标题
    introduction text  comment '简介 可为空',                          -- 描述性文本
    states tinyint default 1 comment '0-下架 1-上架',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 更新时间
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin comment '解决方案表';

-- 子标题表
CREATE TABLE if not exists subtitles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    solution_id INT NOT NULL COMMENT '关联主内容ID',
    subtitle VARCHAR(100) NOT NULL COMMENT '副标题',
    description TEXT COMMENT '描述（可为空）',
    states tinyint default 1 comment '0-下架 1-上架',
    sort_order INT DEFAULT 0 COMMENT '排序字段',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间
    FOREIGN KEY (solution_id) REFERENCES solutions(id) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin comment '子标题表';

-- 联系记录表
CREATE TABLE if not exists contact_us (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,         -- 联系记录ID
    name VARCHAR(255) NOT NULL,                   -- 姓名
    email VARCHAR(255) NOT NULL,                  -- 邮箱
    company VARCHAR(255),                          -- 公司名称
    phone VARCHAR(20),                            -- 电话
    message TEXT NOT NULL,                         -- 消息内容
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 创建时间
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '联系记录表';

 -- 系统配置表
CREATE TABLE system_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键名',
    config_value TEXT NOT NULL COMMENT '配置值',
    description VARCHAR(255) COMMENT '配置描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '系统配置表';

-- 操作日志表
CREATE TABLE operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL COMMENT '操作管理员ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    target_table VARCHAR(50) NOT NULL COMMENT '操作目标表',
    target_id BIGINT COMMENT '操作目标ID',
    operation_detail TEXT COMMENT '操作详情',
    ip_address VARCHAR(45) COMMENT '操作IP',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT '操作日志表';
