-- ================================
--  题库 / 试卷 / 答卷 模块建表脚本
--  创建日期: 2026-04-09
-- ================================

-- 题库表
CREATE TABLE question (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    type          TINYINT                NOT NULL COMMENT '1单选 2多选 3问答',
    content       TEXT                   NOT NULL COMMENT '题干（Markdown）',
    options       JSON                   NULL COMMENT '选项(JSON数组)',
    answer        JSON                   NOT NULL COMMENT '标准答案(JSON)',
    explanation   TEXT                   NULL COMMENT '解析',
    tags          JSON                   NULL COMMENT '标签/知识点',
    difficulty    TINYINT DEFAULT 2      NULL COMMENT '难度 1-5',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_type (type),
    FULLTEXT INDEX idx_content (content)
) COMMENT='题库表';

-- 试卷表
CREATE TABLE paper (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '试卷ID',
    title        VARCHAR(100)           NOT NULL COMMENT '标题',
    total_score  INT                    NOT NULL COMMENT '总分',
    time_limit   INT                    NULL COMMENT '时长(分钟)',
    status       TINYINT  DEFAULT 0     NULL COMMENT '0草稿 1发布',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) COMMENT='试卷';

-- 试卷-题目关联
CREATE TABLE paper_question (
    paper_id     BIGINT NOT NULL COMMENT '试卷ID',
    question_id  BIGINT NOT NULL COMMENT '题目ID',
    seq          INT    NOT NULL COMMENT '在试卷中的顺序',
    score        INT    NOT NULL COMMENT '分值',
    PRIMARY KEY (paper_id, question_id),
    INDEX idx_seq (seq)
) COMMENT='试卷题目关联';

-- 用户一次做卷记录
CREATE TABLE paper_attempt (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '答卷ID',
    paper_id      BIGINT    NOT NULL COMMENT '试卷ID',
    user_id       BIGINT    NOT NULL COMMENT '用户ID',
    start_time    DATETIME  NOT NULL COMMENT '开始时间',
    submit_time   DATETIME  NULL COMMENT '交卷时间',
    total_score   INT       NULL COMMENT '总得分',
    status        TINYINT DEFAULT 0 COMMENT '0进行中 1已交卷',
    INDEX idx_user (user_id),
    INDEX idx_paper (paper_id)
) COMMENT='用户答卷';

-- 用户对每道题的答案
CREATE TABLE paper_answer (
    attempt_id    BIGINT   NOT NULL COMMENT '答卷ID',
    question_id   BIGINT   NOT NULL COMMENT '题目ID',
    user_answer   JSON     NOT NULL COMMENT '用户答案',
    score         INT      NULL COMMENT '得分',
    spend_seconds INT      NULL COMMENT '作答耗时',
    PRIMARY KEY (attempt_id, question_id)
) COMMENT='答卷题目结果';
