create table article
(
    id                     bigint auto_increment comment '文章ID'
        primary key,
    user_id                bigint                               not null comment '作者ID',
    title                  varchar(200)                         not null comment '标题',
    summary                varchar(500)                         null comment '摘要',
    content                longtext                             not null comment '内容',
    content_html           longtext                             null comment 'HTML内容',
    cover_image            varchar(500)                         null comment '封面图',
    category_id            bigint                               null comment '分类ID',
    type                   varchar(1) default '1'               null comment '类型 1：文章，2：笔记，3：项目展示，4：问答讨论',
    format                 varchar(1) default '2'               null comment '格式 1:Markdown 2:富文本',
    status                 varchar(1) default '2'               null comment '状态  1:已发布  2:审核中  3:审核失败  4:删除  5:撤回审核',
    visibility             varchar(1) default '0'               null comment '可见性 0:公开 1:私密 2:仅粉丝',
    is_top                 varchar(1) default '0'               null comment '是否置顶',
    is_recommend           varchar(1) default '0'               null comment '是否推荐',
    is_original            varchar(1) default '0'               null comment '是否原创',
    is_commentable         varchar(1) default '1'               null comment '是否可评论',
    view_count             int        default 0                 null comment '浏览数',
    like_count             int        default 0                 null comment '点赞数',
    comment_count          int        default 0                 null comment '评论数',
    collect_count          int        default 0                 null comment '收藏数',
    share_count            int        default 0                 null comment '分享数',
    word_count             int        default 0                 null comment '字数',
    reading_time           int        default 0                 null comment '阅读时长(分钟)',
    last_comment_time      datetime                             null comment '最后评论时间',
    published_at           datetime                             null comment '发布时间',
    created_at             datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at             datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    recommend_expose_count int        default 0                 null comment '推荐曝光次数'
)
    comment '文章表';

create index idx_category_id
    on article (category_id);

create index idx_published_at
    on article (published_at desc);

create index idx_status
    on article (status);

create index idx_title
    on article (title(100));

create index idx_user_id
    on article (user_id);

create index idx_view_count
    on article (view_count desc);

create table article_tag
(
    id            bigint auto_increment comment '标签ID'
        primary key,
    name          varchar(30)                             not null comment '标签名称',
    slug          varchar(30)                             not null comment '标签标识',
    description   varchar(200)                            null comment '描述',
    color         varchar(20)   default '#409eff'         null comment '标签颜色',
    article_count int           default 0                 null comment '文章数量',
    created_at    datetime      default CURRENT_TIMESTAMP null comment '创建时间',
    category_id   bigint                                  null comment '推荐分类（非强绑定）',
    keyword       varchar(200)                            not null comment '关键词集合（命中规则）',
    stopword      varchar(200)                            null comment '否定词（误判修正）',
    min_hit       int           default 2                 null comment '最少命中次数',
    min_density   decimal(5, 4) default 0.0030            null comment '最小命中密度',
    weight        int           default 10                null comment '标签权重（搜索/推荐）',
    status        varchar(1)    default '1'               null comment '1启用 0禁用',
    constraint slug
        unique (slug)
)
    comment '文章标签表';

create index idx_article_count
    on article_tag (article_count desc);

create index idx_name
    on article_tag (name);

create table article_tag_relation
(
    id         bigint auto_increment comment 'ID'
        primary key,
    article_id bigint                                  not null comment '文章ID',
    tag_id     bigint                                  not null comment '标签ID',
    created_at datetime      default CURRENT_TIMESTAMP null comment '创建时间',
    hit_count  int           default 0                 null comment '命中次数',
    density    decimal(6, 4) default 0.0000            null comment '命中密度',
    score      decimal(6, 2) default 0.00              null comment '成立评分',
    source     varchar(1)    default '1'               null comment '1系统生成 2用户补充',
    confirmed  varchar(1)    default '0'               null comment '是否人工确认',
    constraint uk_article_tag
        unique (article_id, tag_id)
)
    comment '文章标签关联表';

create index idx_article_id
    on article_tag_relation (article_id);

create index idx_tag_id
    on article_tag_relation (tag_id);

create table article_view_history
(
    id                bigint auto_increment comment '记录ID'
        primary key,
    article_id        bigint                             not null comment '文章ID',
    user_id           bigint                             null comment '浏览用户ID（匿名用户为空）',
    viewer_ip         varchar(50)                        null comment '浏览者IP',
    viewer_user_agent varchar(500)                       null comment '浏览者User-Agent',
    view_duration     int      default 0                 null comment '浏览时长(秒)',
    created_at        datetime default CURRENT_TIMESTAMP null comment '浏览时间'
)
    comment '文章浏览记录表';

create index idx_article_id
    on article_view_history (article_id);

create index idx_article_user
    on article_view_history (article_id asc, user_id asc, created_at desc);

create index idx_created_at
    on article_view_history (created_at desc);

create index idx_user_id
    on article_view_history (user_id);

create table audit_log
(
    id         bigint auto_increment
        primary key,
    audit_id   bigint                             not null,
    action     tinyint                            null comment '1自动通过 2自动拒绝 3人工通过 4人工拒绝',
    auditor_id bigint                             null,
    reason     varchar(255)                       null,
    created_at datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '审核历史表';

create table audit_record
(
    id           bigint auto_increment
        primary key,
    biz_type     varchar(32)                        not null comment 'ARTICLE',
    article_id   bigint                             not null comment '文章ID',
    audit_status tinyint                            not null comment '0待审核 1通过 2拒绝',
    audit_type   tinyint                            not null comment '1自动 2人工',
    auditor_id   bigint                             null comment '审核人',
    reason       varchar(255)                       null,
    created_at   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_biz
        unique (biz_type, article_id)
)
    comment '审核表';

create table category
(
    id              bigint auto_increment comment '分类ID'
        primary key,
    parent_id       bigint     default 0                 null comment '父分类ID，0表示顶级分类',
    level           varchar(1) default '1'               not null comment '分类层级 1:一级 2:二级 3:三级',
    name            varchar(50)                          not null comment '分类名称',
    slug            varchar(50)                          not null comment '分类标识（用于URL）',
    icon            varchar(100)                         null comment '图标',
    description     varchar(200)                         null comment '描述',
    cover_image     varchar(500)                         null comment '封面图',
    sort            int        default 0                 null comment '排序',
    article_count   int        default 0                 null comment '文章数量',
    is_recommend    varchar(1) default '0'               null comment '是否推荐',
    is_nav          varchar(1) default '0'               null comment '是否在导航显示',
    status          varchar(1) default '1'               null comment '状态 0:禁用 1:启用',
    seo_title       varchar(200)                         null comment 'SEO标题',
    seo_keywords    varchar(200)                         null comment 'SEO关键词',
    seo_description varchar(500)                         null comment 'SEO描述',
    created_at      datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at      datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint slug
        unique (slug)
)
    comment '分类表';

create index idx_is_nav
    on category (is_nav);

create index idx_level
    on category (level);

create index idx_parent_id
    on category (parent_id);

create index idx_sort
    on category (sort);

create index idx_status
    on category (status);

create table code_snippet
(
    id            bigint auto_increment comment '代码ID'
        primary key,
    user_id       bigint                               not null comment '作者ID',
    title         varchar(200)                         not null comment '标题',
    description   varchar(500)                         null comment '描述',
    code          text                                 not null comment '代码内容',
    language      varchar(50)                          not null comment '编程语言',
    tags          varchar(200)                         null comment '标签',
    filename      varchar(100)                         null comment '文件名',
    visibility    varchar(1) default '0'               null comment '可见性 0:公开 1:私密 2:仅关注',
    is_fork       varchar(1) default '0'               null comment '是否fork',
    fork_from_id  bigint                               null comment '来源代码ID',
    view_count    int        default 0                 null comment '查看次数',
    run_count     int        default 0                 null comment '运行次数',
    fork_count    int        default 0                 null comment 'fork次数',
    like_count    int        default 0                 null comment '点赞数',
    comment_count int        default 0                 null comment '评论数',
    code_size     int        default 0                 null comment '代码大小(字节)',
    line_count    int        default 0                 null comment '行数',
    created_at    datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at    datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '代码片段表';

create index idx_created_at
    on code_snippet (created_at desc);

create index idx_language
    on code_snippet (language);

create index idx_user_id
    on code_snippet (user_id);

create index idx_view_count
    on code_snippet (view_count desc);

create table comment
(
    id                  bigint auto_increment comment '评论ID'
        primary key,
    user_id             bigint                               not null comment '评论用户ID',
    article_id          bigint                               not null comment '文章ID',
    parent_id           bigint     default 0                 null comment '父评论ID',
    reply_to_user_id    bigint                               null comment '回复的用户ID',
    reply_to_comment_id bigint                               null comment '回复的评论ID',
    content             text                                 not null comment '评论内容',
    content_html        text                                 null comment 'HTML内容',
    like_count          int        default 0                 null comment '点赞数',
    dislike_count       int        default 0                 null comment '点踩数',
    status              varchar(1) default '1'               null comment '状态 0:删除 1:正常 2:审核',
    is_author           varchar(1) default '0'               null comment '是否作者回复',
    ip                  varchar(50)                          null comment 'IP地址',
    user_agent          varchar(500)                         null comment '用户代理',
    created_at          datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at          datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    nickname            varchar(50)                          null comment '用户昵称',
    avatar              varchar(500)                         null comment '用户头像URL',
    root_id             bigint     default 0                 null comment '所属一级评论ID',
    reply_count         int        default 0                 null comment '子评论数量'
)
    comment '评论表';

create index idx_article_id
    on comment (article_id);

create index idx_article_root
    on comment (article_id asc, root_id asc, id desc);

create index idx_created_at
    on comment (created_at desc);

create index idx_parent_id
    on comment (parent_id);

create index idx_root_created
    on comment (root_id, created_at);

create index idx_status
    on comment (status);

create index idx_user_id
    on comment (user_id);

create table comment_like_record
(
    id         bigint auto_increment comment '记录ID'
        primary key,
    user_id    bigint                               not null comment '点赞用户ID',
    comment_id bigint                               not null comment '评论ID',
    status     varchar(1) default '1'               not null comment '1=点赞 0=取消',
    created_at datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_user_comment
        unique (user_id, comment_id)
)
    comment '评论点赞表';

create index idx_comment_id
    on comment_like_record (comment_id);

create index idx_created_at
    on comment_like_record (created_at desc);

create table dictionary
(
    id          int auto_increment
        primary key,
    dict_type   varchar(50)                         not null,
    dict_value  varchar(100)                        not null,
    description varchar(255)                        not null,
    dict_sort   int       default 0                 not null,
    created_at  timestamp default CURRENT_TIMESTAMP null,
    updated_at  timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
);

create table draft
(
    id               bigint auto_increment
        primary key,
    user_id          bigint                                 not null comment '用户ID',
    title            varchar(200) default ''                null comment '标题',
    summary          varchar(500) default ''                null comment '摘要/简介',
    content          longtext                               null comment '内容（HTML格式）',
    content_raw      longtext                               null comment '原始内容（Markdown等）',
    content_type     varchar(20)  default 'html'            null comment '内容类型:html/markdown',
    cover_image      varchar(500)                           null comment '封面图URL',
    module_type      varchar(1)                             not null comment '模块类型:1文章/2问答/3笔记/4项目',
    module_id        bigint       default 0                 null comment '关联正式内容ID（编辑时）',
    category_id      bigint       default 0                 null comment '分类ID',
    tags             varchar(255)                           null comment '标签，多个用逗号分隔',
    visibility       varchar(1)   default '0'               null comment '可见性:0公开 1私密 2仅粉丝 3密码访问',
    password         varchar(100)                           null comment '访问密码（当visibility=3时）',
    format           tinyint      default 1                 null comment '格式:1富文本 2Markdown',
    language         varchar(20)  default 'zh-CN'           null comment '语言',
    copyright        varchar(50)  default '原创'            null comment '版权类型:原创/转载/翻译',
    original_url     varchar(500)                           null comment '原文链接（转载时）',
    question_type    varchar(50)                            null comment '问题类型:技术/求职/生活等',
    reward_points    int          default 0                 null comment '悬赏积分',
    best_answer_id   bigint       default 0                 null comment '最佳答案ID',
    project_type     varchar(50)                            null comment '项目类型:开源/商业/个人',
    tech_stack       varchar(255)                           null comment '技术栈，多个用逗号分隔',
    github_url       varchar(200)                           null comment 'GitHub地址',
    demo_url         varchar(200)                           null comment '演示地址',
    project_status   varchar(20)  default 'ongoing'         null comment '项目状态:ongoing/completed/archived',
    word_count       int          default 0                 null comment '字数',
    reading_time     int          default 0                 null comment '阅读时长（分钟）',
    char_count       int          default 0                 null comment '字符数',
    image_count      int          default 0                 null comment '图片数量',
    code_block_count int          default 0                 null comment '代码块数量',
    auto_saved       varchar(1)   default '1'               null comment '是否自动保存 0:否 1:是',
    is_temporary     varchar(1)   default '0'               null comment '是否临时草稿 0:否 1:是',
    temp_expire_at   datetime                               null comment '临时草稿过期时间',
    save_reason      varchar(100)                           null comment '保存原因:auto/timed/manual',
    version          int          default 1                 null comment '版本号',
    parent_draft_id  bigint       default 0                 null comment '父草稿ID',
    change_log       varchar(500)                           null comment '版本变更说明',
    last_modified_at datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '最后修改时间',
    created_at       datetime     default CURRENT_TIMESTAMP null comment '创建时间',
    extra_data       text                                   null comment '额外数据，存储编辑器状态、自定义设置等'
)
    comment '草稿表' collate = utf8mb4_unicode_ci;

create index idx_last_modified
    on draft (last_modified_at desc);

create index idx_user_temp_time
    on draft (user_id, is_temporary, last_modified_at);

create table favorite
(
    id          bigint auto_increment comment 'ID'
        primary key,
    user_id     bigint                               not null comment '用户ID',
    target_type varchar(1)                           not null comment '目标类型 1:文章 2:问题 3:代码',
    target_id   bigint                               not null comment '目标ID',
    folder_id   bigint     default 0                 not null comment '收藏夹ID (0=默认收藏夹)',
    status      varchar(1) default '1'               not null comment '1=收藏 0=取消收藏',
    created_at  datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at  datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_user_target_type
        unique (user_id, target_type, target_id),
    constraint chk_status
        check (`status` in (_utf8mb4\'0\',_utf8mb4\'1\')),
	constraint chk_target_type
		check (`target_type` in (_utf8mb4\'1\',_utf8mb4\'2\',_utf8mb4\'3\'))
)
comment '收藏表';

create table favorite_folder
(
    id          bigint auto_increment comment '收藏夹ID'
        primary key,
    user_id     bigint                               not null comment '用户ID',
    name        varchar(50)                          not null comment '收藏夹名称',
    description varchar(200)                         null comment '描述',
    cover_image varchar(500)                         null comment '封面图',
    is_public   varchar(1) default '1'               null comment '是否公开 0:私密 1:公开',
    is_default  varchar(1) default '0'               null comment '是否为默认收藏夹',
    item_count  int        default 0                 null comment '收藏项数量',
    sort        int        default 0                 null comment '排序',
    created_at  datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at  datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_user_folder_name
        unique (user_id, name)
)
    comment '用户收藏夹表';

create index idx_is_default
    on favorite_folder (is_default);

create index idx_user_id
    on favorite_folder (user_id);

create table follow
(
    id           bigint auto_increment comment 'ID'
        primary key,
    follower_id  bigint                             not null comment '关注者ID',
    following_id bigint                             not null comment '被关注者ID',
    created_at   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint uk_follow
        unique (follower_id, following_id)
)
    comment '关注关系表';

create index idx_follower_id
    on follow (follower_id);

create index idx_following_id
    on follow (following_id);

create table like_record
(
    id           bigint auto_increment
        primary key,
    user_id      bigint                               not null comment '用户ID',
    article_id   bigint                               not null comment '文章ID',
    article_type varchar(1)                           not null comment '文章类型',
    status       varchar(1) default '1'               not null comment '1=点赞 0=取消',
    create_at    datetime   default CURRENT_TIMESTAMP null,
    update_at    datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_user_article
        unique (user_id, article_id, article_type)
)
    comment '点赞状态表';

create index idx_article
    on like_record (article_id, article_type);

create index idx_update_time
    on like_record (update_at);

create table notification
(
    id           bigint auto_increment comment '通知ID'
        primary key,
    user_id      bigint                                not null comment '接收用户ID',
    from_user_id bigint                                null comment '发送用户ID',
    type         varchar(20)                           not null comment '类型 system:系统消息 user:用户互动 message:私信提醒',
    action_type  varchar(50)                           null comment '动作类型（用于细分，如like_article, like_comment, follow, reply等）',
    title        varchar(200)                          null comment '标题',
    content      varchar(500)                          null comment '内容',
    target_type  varchar(50)                           null comment '目标类型',
    target_id    bigint                                null comment '目标ID',
    parent_id    bigint                                null comment '父级ID（如回复的评论ID）',
    extra_data   json                                  null comment '额外数据',
    is_read      varchar(1)  default '0'               null comment '是否已读',
    created_at   datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    read_at      datetime                              null comment '阅读时间',
    source_id    bigint                                null comment '来源对象ID(如评论ID/文章ID)',
    source_type  varchar(50)                           null comment '来源对象类型(comment/article/message)',
    group_id     varchar(100)                          null comment '通知聚合ID',
    is_deleted   varchar(1)  default '0'               null comment '是否删除',
    notify_level varchar(20) default 'normal'          null comment '通知级别(normal/important)'
)
    comment '通知表';

create index idx_created_at
    on notification (created_at desc);

create index idx_is_read
    on notification (is_read);

create index idx_source
    on notification (source_type, source_id);

create index idx_type
    on notification (type);

create index idx_user_action
    on notification (user_id asc, action_type asc, created_at desc);

create index idx_user_group
    on notification (user_id, group_id);

create index idx_user_id
    on notification (user_id);

create index idx_user_type_read
    on notification (user_id asc, type asc, is_read asc, created_at desc);

create table permission
(
    id              bigint auto_increment comment '权限ID'
        primary key,
    parent_id       bigint     default 0                 null comment '父权限ID',
    name            varchar(100)                         not null comment '权限名称',
    permission_code varchar(100)                         not null comment '权限编码',
    type            varchar(20)                          not null comment '权限类型 MENU:菜单 BUTTON:按钮 API:接口',
    path            varchar(200)                         null comment '前端路由路径',
    component       varchar(200)                         null comment '前端组件路径',
    icon            varchar(100)                         null comment '菜单图标',
    method          varchar(20)                          null comment '请求方法 GET POST PUT DELETE',
    api_path        varchar(200)                         null comment '接口路径',
    sort            int        default 0                 null comment '排序',
    status          varchar(1) default '1'               null comment '状态 0:禁用 1:启用',
    created_at      datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at      datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_permission_code
        unique (permission_code)
)
    comment '权限表';

create table private_conversation
(
    id                bigint auto_increment
        primary key,
    user_min          bigint                             not null,
    user_max          bigint                             not null,
    last_message_id   bigint                             null,
    last_message_time datetime                           null,
    created_at        datetime default CURRENT_TIMESTAMP null,
    updated_at        datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint user_min
        unique (user_min, user_max)
);

create table private_message
(
    id              bigint auto_increment
        primary key,
    conversation_id bigint                               not null,
    sender_id       bigint                               not null,
    receiver_id     bigint                               not null,
    content         text                                 not null,
    content_type    varchar(1) default '1'               null,
    is_read         tinyint    default 0                 null,
    created_at      datetime   default CURRENT_TIMESTAMP null,
    is_deleted      tinyint    default 0                 null
);

create table report
(
    id             bigint auto_increment comment '举报ID'
        primary key,
    reporter_id    bigint                                not null comment '举报人ID',
    target_type    varchar(20)                           not null comment '举报目标类型：article-文章, comment-评论, user-用户',
    target_id      bigint                                not null comment '举报目标ID',
    target_user_id bigint                                not null comment '被举报用户ID',
    reason_type    varchar(20)                           not null comment '举报原因类型：spam-垃圾信息, inappropriate-不当内容, harassment-骚扰, copyright-版权, other-其他',
    reason_detail  varchar(500)                          null comment '详细举报原因',
    evidence_urls  text                                  null comment '举报证据图片URLs，多个用逗号分隔',
    status         varchar(10) default 'pending'         not null comment '处理状态：pending-待处理, processing-处理中, resolved-已处理, rejected-已驳回',
    admin_id       bigint                                null comment '处理管理员ID',
    admin_remark   varchar(500)                          null comment '管理员处理备注',
    processed_at   datetime                              null comment '处理时间',
    created_at     datetime    default CURRENT_TIMESTAMP not null comment '举报时间',
    updated_at     datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '举报表';

create index idx_created_at
    on report (created_at);

create index idx_reporter_id
    on report (reporter_id);

create index idx_status
    on report (status);

create index idx_target_type_id
    on report (target_type, target_id);

create index idx_target_user_id
    on report (target_user_id);

create table report_reason
(
    id           bigint auto_increment comment '原因ID'
        primary key,
    code         varchar(20)                           not null comment '原因代码',
    name         varchar(50)                           not null comment '原因名称',
    description  varchar(200)                          null comment '原因描述',
    target_types varchar(100)                          not null comment '适用的目标类型，多个用逗号分隔',
    sort         int         default 0                 null comment '排序',
    status       varchar(10) default 'active'          null comment '状态：active-启用, inactive-禁用',
    created_at   datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at   datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_code
        unique (code)
)
    comment '举报原因配置表';

create index idx_sort
    on report_reason (sort);

create index idx_status
    on report_reason (status);

create table role
(
    id          bigint auto_increment comment '角色ID'
        primary key,
    role_name   varchar(50)                          not null comment '角色名称',
    role_code   varchar(50)                          not null comment '角色编码',
    description varchar(200)                         null comment '描述',
    status      varchar(1) default '1'               null comment '状态 0:禁用 1:启用',
    created_at  datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    sort        int        default 1                 null comment '排序号',
    updated_at  datetime   default CURRENT_TIMESTAMP null comment '更新时间',
    constraint role_code
        unique (role_code)
)
    comment '角色表';

create table role_permission
(
    id            bigint auto_increment comment 'ID'
        primary key,
    role_id       bigint                             not null comment '角色ID',
    permission_id bigint                             not null comment '权限ID',
    created_at    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint uk_role_permission
        unique (role_id, permission_id)
)
    comment '角色权限关联表';

create table search_history
(
    id               bigint auto_increment comment 'ID'
        primary key,
    user_id          bigint                             not null comment '用户ID',
    search_term      varchar(255)                       not null comment '搜索词',
    searched_at      datetime default CURRENT_TIMESTAMP null comment '搜索时间',
    last_searched_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '最后搜索时间',
    constraint uk_user_search_term
        unique (user_id, search_term)
)
    comment '用户搜索历史表';

create index idx_search_term
    on search_history (search_term(50));

create index idx_searched_at
    on search_history (searched_at desc);

create index idx_user_id
    on search_history (user_id);

create index idx_user_search
    on search_history (user_id asc, searched_at desc);

create table search_hot
(
    id           int auto_increment
        primary key,
    search_term  varchar(255)                       not null comment '搜索词',
    search_count int      default 0                 null comment '搜索次数',
    updated_at   datetime default CURRENT_TIMESTAMP null,
    created_at   datetime default CURRENT_TIMESTAMP null
)
    comment '热门搜索表';

create table sign_in_log
(
    id              bigint auto_increment comment 'ID'
        primary key,
    user_id         bigint                             not null comment '用户ID',
    date            date                               not null comment '签到日期',
    continuous_days int      default 1                 null comment '连续签到天数',
    score_award     int      default 10                null comment '获得积分',
    created_at      datetime default CURRENT_TIMESTAMP null comment '创建时间',
    constraint uk_user_date
        unique (user_id, date)
)
    comment '签到记录表';

create index idx_date
    on sign_in_log (date);

create index idx_user_id
    on sign_in_log (user_id);

create table system_config
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    config_key   varchar(100)                          not null comment '配置键（唯一标识）',
    config_value text                                  null comment '配置值',
    value_type   varchar(20) default 'string'          null comment '类型 string/number/boolean/json',
    group_name   varchar(50)                           not null comment '分组(system/feature/security/email/upload)',
    config_name  varchar(100)                          null comment '配置名称（用于前端显示）',
    description  varchar(255)                          null comment '描述',
    is_public    tinyint     default 0                 null comment '是否前端可见 0否 1是',
    sort         int         default 0                 null comment '排序',
    status       varchar(1)  default '1'               null comment '状态 0禁用 1启用',
    created_at   datetime    default CURRENT_TIMESTAMP null,
    updated_at   datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_config_key
        unique (config_key)
)
    comment '系统配置表';

create index idx_group
    on system_config (group_name);

create table system_config_group
(
    id          bigint auto_increment
        primary key,
    group_name  varchar(50)            not null comment '分组标识',
    group_label varchar(100)           null comment '分组名称（显示用）',
    sort        int        default 0   null,
    status      varchar(1) default '1' null,
    constraint uk_group_name
        unique (group_name)
)
    comment '配置分组表';

create table user
(
    id               bigint auto_increment comment '用户ID'
        primary key,
    username         varchar(50)                          not null comment '用户名',
    password         varchar(255)                         not null comment '密码',
    email            varchar(100)                         null comment '邮箱',
    phone            varchar(20)                          null comment '手机号',
    nickname         varchar(50)                          null comment '昵称',
    avatar           varchar(500)                         null comment '头像URL',
    cover_images     varchar(500)                         null comment '背景封面图片URL字符串（多个以逗号分隔）',
    gender           varchar(1) default '0'               null comment '性别 0:未知 1:男 2:女',
    birthday         date                                 null comment '生日',
    introduction     varchar(500)                         null comment '个人简介',
    website          varchar(200)                         null comment '个人网站',
    location         varchar(100)                         null comment '所在地',
    company          varchar(100)                         null comment '公司',
    position         varchar(100)                         null comment '职位',
    signature        varchar(200)                         null comment '个性签名',
    score            int        default 0                 null comment '积分',
    level            int        default 1                 null comment '等级',
    experience       int        default 0                 null comment '经验值',
    gold             int        default 0                 null comment '金币',
    topic_count      int        default 0                 null comment '发帖数',
    comment_count    int        default 0                 null comment '评论数',
    follower_count   int        default 0                 null comment '粉丝数',
    following_count  int        default 0                 null comment '关注数',
    collection_count int        default 0                 null comment '收藏数',
    status           varchar(1) default '0'               null comment '状态 0:正常 1:禁用 2:未激活',
    email_verified   varchar(1) default '0'               null comment '邮箱验证',
    phone_verified   varchar(1) default '0'               null comment '手机验证',
    last_login_time  datetime                             null comment '最后登录时间',
    last_login_ip    varchar(50)                          null comment '最后登录IP',
    created_at       datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at       datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint email
        unique (email),
    constraint phone
        unique (phone),
    constraint username
        unique (username)
)
    comment '用户表';

create index idx_email
    on user (email);

create index idx_score
    on user (score desc);

create index idx_status
    on user (status);

create index idx_username
    on user (username);

create table user_behavior_log
(
    id              bigint auto_increment
        primary key,
    user_id         bigint                             not null comment '用户ID',
    article_id      bigint                             not null comment '文章ID',
    behavior_type   tinyint                            not null comment '1浏览 2点赞 3收藏 4评论 5分享',
    behavior_weight int                                not null comment '行为基础权重',
    tag_id          bigint                             null comment '标签ID',
    category_id     bigint                             null comment '分类ID',
    author_id       bigint                             null comment '作者ID',
    create_at       datetime default CURRENT_TIMESTAMP null comment '行为时间'
)
    comment '用户行为日志表';

create index idx_user_behavior
    on user_behavior_log (user_id, behavior_type);

create index idx_user_tag
    on user_behavior_log (user_id, tag_id);

create index idx_user_time
    on user_behavior_log (user_id, create_at);

create table user_oauth
(
    id           bigint auto_increment
        primary key,
    user_id      bigint                             not null,
    oauth_type   varchar(20)                        not null,
    open_id      varchar(100)                       not null,
    access_token varchar(200)                       null,
    create_time  datetime default CURRENT_TIMESTAMP null,
    constraint uk_oauth
        unique (oauth_type, open_id)
);

create table user_privacy
(
    id                            bigint auto_increment comment '隐私设置ID'
        primary key,
    user_id                       bigint                               not null comment '用户ID',
    email_visibility              varchar(1) default '0'               null comment '邮箱可见性 0: 公开 1: 私密',
    phone_visibility              varchar(1) default '0'               null comment '手机号可见性 0: 公开 1: 私密',
    profile_visibility            varchar(1) default '0'               null comment '个人主页可见性 0: 公开 1: 私密 2: 粉丝可见 3: 关注可见',
    can_comment                   varchar(1) default '0'               null comment '是否可评论 0: 全部可以 1：仅自己评论 2：粉丝可评论 3：互相关注可评论',
    article_visibility            varchar(1) default '0'               null comment '文章可见性 0：公开 1：私密 2：粉丝可见 3：互相关注可见',
    created_at                    datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at                    datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    likes_hidden                  varchar(1) default '0'               null comment '喜欢列表是否隐藏 0: 显示 1: 隐藏',
    favorites_hidden              varchar(1) default '0'               null comment '收藏列表是否隐藏 0: 显示 1: 隐藏',
    follow_list_hidden            varchar(1) default '0'               null comment '关注列表是否隐藏 0: 显示 1: 隐藏',
    followers_list_hidden         varchar(1) default '0'               null comment '粉丝列表是否隐藏 0: 显示 1: 隐藏',
    allow_private_message         varchar(1) default '0'               null comment '互动允许私信 0: 所有人 1: 仅粉丝 2: 仅互相关注 3: 禁止',
    allow_mention                 varchar(1) default '0'               null comment '允许@提及 0: 是 1: 否',
    new_follower_notification     varchar(1) default '0'               null comment '新粉丝通知 0: 是 1: 否',
    allow_recommendation          varchar(1) default '0'               null comment '允许推荐作品 0: 是 1: 否',
    interest_based_recommendation varchar(1) default '0'               null comment '基于兴趣推荐内容 0: 是 1: 否',
    data_analysis                 varchar(1) default '0'               null comment '数据分析 0: 是 1: 否',
    third_party_data_sharing      varchar(1) default '0'               null comment '第三方数据共享 0: 是 1: 否'
)
    comment '用户隐私表';

create table user_profile
(
    id                 bigint auto_increment
        primary key,
    user_id            bigint                             not null comment '用户ID',
    tag_profile        json                               null comment '标签兴趣画像',
    category_profile   json                               null comment '分类兴趣画像',
    author_profile     json                               null comment '作者兴趣画像',
    behavior_count     int      default 0                 null comment '有效行为数',
    profile_level      tinyint  default 0                 null comment '画像质量等级 0：无画像 1：弱画像，2：稳定画像，3：强画像',
    last_calculated_at datetime                           null comment '最近画像计算时间',
    create_at          datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_at          datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_user
        unique (user_id)
)
    comment '用户画像表';

create table user_role
(
    id         bigint auto_increment comment 'ID'
        primary key,
    user_id    bigint                             not null comment '用户ID',
    role_id    bigint                             not null comment '角色ID',
    created_at datetime default CURRENT_TIMESTAMP null comment '创建时间',
    role_name  varchar(255)                       null comment '角色名称',
    constraint uk_user_role
        unique (user_id, role_id)
)
    comment '用户角色关联表';

create index idx_role_id
    on user_role (role_id);

create index idx_user_id
    on user_role (user_id);

INSERT INTO `user` (`id`, `username`, `password`, `email`, `phone`, `nickname`, `avatar`, `cover_images`, `gender`, `birthday`, `introduction`, `website`, `location`, `company`, `position`, `signature`, `score`, `level`, `experience`, `gold`, `topic_count`, `comment_count`, `follower_count`, `following_count`, `collection_count`, `status`, `email_verified`, `phone_verified`, `last_login_time`, `last_login_ip`, `created_at`, `updated_at`) VALUES
(70, 'user_70', 'e10adc3949ba59ab', 'user70@example.com', '13800000070', '清风徐来', 'http://127.0.0.1:9000/avatar/70.jpg', 'http://127.0.0.1:9000/cover/70_1.jpg,http://127.0.0.1:9000/cover/70_2.jpg', '1', '1995-03-21', '热爱生活，热爱编程', 'https://blog.example.com', '北京市朝阳区', '字节跳动', '后端开发工程师', '代码改变世界', 1560, 8, 3500, 230, 45, 128, 230, 180, 76, '0', '1', '1', '2026-03-17 10:23:45', '192.168.1.70', '2025-06-15 08:30:00', '2026-03-17 10:23:45'),
(71, 'user_71', 'e10adc3949ba59ab', 'user71@example.com', '13800000071', '明月照沟渠', 'http://127.0.0.1:9000/avatar/71.jpg', 'http://127.0.0.1:9000/cover/71_1.jpg', '2', '1992-08-12', '摄影爱好者，美食家', 'http://photo.art', '上海市浦东新区', '小红书', '内容运营', '记录美好生活', 2340, 12, 5800, 450, 67, 342, 560, 320, 189, '0', '1', '1', '2026-03-16 22:15:33', '192.168.1.71', '2025-03-20 14:25:00', '2026-03-16 22:15:33'),
(72, 'user_72', 'e10adc3949ba59ab', 'user72@example.com', '13800000072', '江南烟雨', 'http://127.0.0.1:9000/avatar/72.jpg', 'http://127.0.0.1:9000/cover/72_1.jpg,http://127.0.0.1:9000/cover/72_2.jpg', '1', '1998-11-05', '旅行博主，分享世界美景', 'https://travel.com', '杭州市西湖区', '阿里巴巴', '产品经理', '读万卷书，行万里路', 890, 5, 2100, 120, 23, 56, 89, 120, 34, '0', '1', '0', '2026-03-15 14:30:22', '192.168.1.72', '2025-09-10 09:45:00', '2026-03-15 14:30:22'),
(73, 'user_73', 'e10adc3949ba59ab', 'user73@example.com', '13800000073', '北冥有鱼', 'http://127.0.0.1:9000/avatar/73.jpg', NULL, '1', '1991-07-19', '全栈开发，开源爱好者', 'https://github.com', '深圳市南山区', '腾讯科技', '高级开发工程师', 'Talk is cheap. Show me the code.', 3450, 15, 8900, 890, 156, 423, 890, 450, 234, '0', '1', '1', '2026-03-17 09:12:08', '192.168.1.73', '2024-12-01 11:20:00', '2026-03-17 09:12:08'),
(74, 'user_74', 'e10adc3949ba59ab', 'user74@example.com', '13800000074', '南山南', 'http://127.0.0.1:9000/avatar/74.jpg', 'http://127.0.0.1:9000/cover/74_1.jpg', '2', '1993-12-24', '音乐老师，钢琴演奏', 'https://music.com', '广州市天河区', '星海音乐学院', '钢琴教师', '音乐是灵魂的语言', 1200, 6, 2800, 340, 34, 89, 150, 95, 45, '0', '1', '1', '2026-03-16 20:45:12', '192.168.1.74', '2025-07-22 13:15:00', '2026-03-16 20:45:12'),
(75, 'user_75', 'e10adc3949ba59ab', 'user75@example.com', '13800000075', '北海有墓碑', 'http://127.0.0.1:9000/avatar/75.jpg', NULL, '1', '1996-09-30', '健身教练，营养师', 'https://fitness.com', '成都市武侯区', '超级猩猩', '私人教练', '自律给我自由', 780, 4, 1650, 90, 12, 34, 67, 89, 23, '0', '0', '1', '2026-03-15 18:30:55', '192.168.1.75', '2025-10-05 16:40:00', '2026-03-15 18:30:55'),
(76, 'user_76', 'e10adc3949ba59ab', 'user76@example.com', '13800000076', '长安一片月', 'http://127.0.0.1:9000/avatar/76.jpg', 'http://127.0.0.1:9000/cover/76_1.jpg,http://127.0.0.1:9000/cover/76_2.jpg', '2', '1994-04-15', 'UI设计师，插画师', 'https://behance.net', '西安市雁塔区', '网易游戏', 'UI设计师', '设计源于生活', 1890, 9, 4200, 340, 45, 78, 230, 170, 98, '0', '1', '1', '2026-03-16 11:05:34', '192.168.1.76', '2025-05-18 10:30:00', '2026-03-16 11:05:34'),
(77, 'user_77', 'e10adc3949ba59ab', 'user77@example.com', '13800000077', '洛阳花下客', 'http://127.0.0.1:9000/avatar/77.jpg', NULL, '1', '1990-09-08', '律师，法律咨询', 'https://law.com', '郑州市金水区', '大成律师事务所', '合伙人律师', '以事实为依据，以法律为准绳', 2100, 10, 5100, 560, 23, 45, 340, 210, 67, '0', '1', '1', '2026-03-17 08:45:21', '192.168.1.77', '2024-11-12 15:20:00', '2026-03-17 08:45:21'),
(78, 'user_78', 'e10adc3949ba59ab', 'user78@example.com', '13800000078', '姑苏城外', 'http://127.0.0.1:9000/avatar/78.jpg', 'http://127.0.0.1:9000/cover/78_1.jpg', '2', '1997-06-28', '咖啡师，甜品达人', 'https://coffee.com', '苏州市姑苏区', '星巴克', '咖啡大师', '一杯咖啡，一段故事', 560, 3, 1200, 80, 8, 23, 45, 67, 12, '0', '1', '0', '2026-03-14 15:22:43', '192.168.1.78', '2025-12-01 08:15:00', '2026-03-14 15:22:43'),
(79, 'user_79', 'e10adc3949ba59ab', 'user79@example.com', '13800000079', '洞庭波', 'http://127.0.0.1:9000/avatar/79.jpg', 'http://127.0.0.1:9000/cover/79_1.jpg,http://127.0.0.1:9000/cover/79_2.jpg', '1', '1989-10-10', '企业培训师，管理咨询', 'https://training.com', '长沙市岳麓区', '三一重工', '培训经理', '授人以鱼不如授人以渔', 2780, 13, 6700, 670, 89, 156, 430, 290, 145, '0', '1', '1', '2026-03-16 13:38:19', '192.168.1.79', '2024-08-25 09:30:00', '2026-03-16 13:38:19'),
(80, 'user_80', 'e10adc3949ba59ab', 'user80@example.com', '13800000080', '秦淮河畔', 'http://127.0.0.1:9000/avatar/80.jpg', NULL, '2', '1995-12-03', '美食博主，探店达人', 'https://foodie.com', '南京市秦淮区', '美食自媒体', '主理人', '唯有美食与爱不可辜负', 1450, 7, 3200, 210, 56, 134, 320, 180, 89, '0', '1', '1', '2026-03-15 19:47:32', '192.168.1.80', '2025-07-08 12:45:00', '2026-03-15 19:47:32'),
(81, 'user_81', 'e10adc3949ba59ab', 'user81@example.com', '13800000081', '西湖瘦', 'http://127.0.0.1:9000/avatar/81.jpg', 'http://127.0.0.1:9000/cover/81_1.jpg', '1', '1992-02-28', '健身博主，马拉松爱好者', 'https://runner.com', '扬州市广陵区', '自由职业', '健身教练', '跑过的每一步都算数', 1890, 9, 4300, 340, 45, 67, 190, 150, 56, '0', '1', '1', '2026-03-17 07:52:11', '192.168.1.81', '2025-04-17 16:20:00', '2026-03-17 07:52:11'),
(82, 'user_82', 'e10adc3949ba59ab', 'user82@example.com', '13800000082', '黄山云海', 'http://127.0.0.1:9000/avatar/82.jpg', 'http://127.0.0.1:9000/cover/82_1.jpg,http://127.0.0.1:9000/cover/82_2.jpg', '2', '1988-08-18', '摄影师，旅游达人', 'https://photo.com', '黄山市屯溪区', '国家地理', '签约摄影师', '用镜头记录世界', 3210, 14, 7800, 780, 123, 234, 560, 320, 178, '0', '1', '1', '2026-03-16 16:28:45', '192.168.1.82', '2024-09-30 10:10:00', '2026-03-16 16:28:45'),
(83, 'user_83', 'e10adc3949ba59ab', 'user83@example.com', '13800000083', '庐山烟雨', 'http://127.0.0.1:9000/avatar/83.jpg', NULL, '1', '1996-11-11', '数据分析师，Python爱好者', 'https://data.com', '南昌市红谷滩区', '华为技术', '数据分析师', '数据驱动决策', 980, 5, 2300, 130, 23, 45, 89, 110, 34, '0', '1', '0', '2026-03-15 12:09:37', '192.168.1.83', '2025-10-22 13:30:00', '2026-03-15 12:09:37'),
(84, 'user_84', 'e10adc3949ba59ab', 'user84@example.com', '13800000084', '峨眉月', 'http://127.0.0.1:9000/avatar/84.jpg', 'http://127.0.0.1:9000/cover/84_1.jpg', '2', '1993-05-20', '瑜伽教练，冥想导师', 'https://yoga.com', '乐山市市中区', '瑜伽馆', '主理人', '身心合一', 1340, 6, 2900, 190, 34, 56, 130, 95, 42, '0', '1', '1', '2026-03-16 20:14:23', '192.168.1.84', '2025-06-05 09:15:00', '2026-03-16 20:14:23'),
(85, 'user_85', 'e10adc3949ba59ab', 'user85@example.com', '13800000085', '泰山石敢当', 'http://127.0.0.1:9000/avatar/85.jpg', 'http://127.0.0.1:9000/cover/85_1.jpg,http://127.0.0.1:9000/cover/85_2.jpg', '1', '1987-07-07', '登山向导，户外探险', 'https://outdoor.com', '泰安市泰山区', '户外俱乐部', '创始人', '征服每一座高峰', 2650, 12, 6200, 580, 78, 89, 340, 230, 123, '0', '1', '1', '2026-03-17 06:42:58', '192.168.1.85', '2024-07-14 08:45:00', '2026-03-17 06:42:58'),
(86, 'user_86', 'e10adc3949ba59ab', 'user86@example.com', '13800000086', '华山论剑', 'http://127.0.0.1:9000/avatar/86.jpg', NULL, '2', '1994-09-15', '武术教练，太极传人', 'https://wushu.com', '渭南市华阴市', '武术学校', '总教练', '以武会友', 1120, 5, 2500, 160, 23, 34, 89, 67, 28, '0', '1', '0', '2026-03-14 17:33:19', '192.168.1.86', '2025-08-19 14:20:00', '2026-03-14 17:33:19'),
(87, 'user_87', 'e10adc3949ba59ab', 'user87@example.com', '13800000087', '衡山云雾', 'http://127.0.0.1:9000/avatar/87.jpg', 'http://127.0.0.1:9000/cover/87_1.jpg', '1', '1991-12-12', '茶艺师，茶叶品鉴', 'https://tea.com', '衡阳市南岳区', '茶叶公司', '品茶师', '一叶知秋', 1560, 7, 3600, 280, 45, 67, 120, 100, 54, '0', '1', '1', '2026-03-16 09:27:44', '192.168.1.87', '2025-05-27 11:30:00', '2026-03-16 09:27:44'),
(88, 'user_88', 'e10adc3949ba59ab', 'user88@example.com', '13800000088', '恒山雪', 'http://127.0.0.1:9000/avatar/88.jpg', 'http://127.0.0.1:9000/cover/88_1.jpg,http://127.0.0.1:9000/cover/88_2.jpg', '2', '1989-01-22', '滑雪教练，冬季运动', 'https://ski.com', '大同市浑源县', '滑雪场', '教练主管', '雪地上的舞蹈', 2030, 10, 4900, 450, 67, 89, 230, 160, 87, '0', '1', '1', '2026-03-15 22:08:15', '192.168.1.88', '2024-10-08 15:40:00', '2026-03-15 22:08:15'),
(89, 'user_89', 'e10adc3949ba59ab', 'user89@example.com', '13800000089', '嵩山少林', 'http://127.0.0.1:9000/avatar/89.jpg', NULL, '1', '1986-06-06', '禅修导师，少林功夫传人', 'https://chan.com', '登封市少林寺', '少林寺', '武僧', '禅武合一', 1780, 8, 4100, 390, 34, 45, 210, 140, 65, '0', '1', '1', '2026-03-17 11:36:52', '192.168.1.89', '2024-06-18 10:00:00', '2026-03-17 11:36:52'),
(90, 'user_90', 'e10adc3949ba59ab', 'user90@example.com', '13800000090', '桃花岛主', 'http://127.0.0.1:9000/avatar/90.jpg', 'http://127.0.0.1:9000/cover/90_1.jpg', '1', '1990-03-03', '园艺师，植物爱好者', 'https://garden.com', '舟山市普陀区', '园艺公司', '园艺设计师', '与植物对话', 890, 4, 1900, 110, 12, 23, 56, 78, 21, '0', '0', '1', '2026-03-13 14:18:37', '192.168.1.90', '2025-09-15 16:50:00', '2026-03-13 14:18:37'),
(91, 'user_91', 'e10adc3949ba59ab', 'user91@example.com', '13800000091', '绝情谷主', 'http://127.0.0.1:9000/avatar/91.jpg', 'http://127.0.0.1:9000/cover/91_1.jpg,http://127.0.0.1:9000/cover/91_2.jpg', '2', '1995-07-07', '心理咨询师', 'https://psych.com', '武汉市洪山区', '心理咨询中心', '心理咨询师', '倾听你的故事', 2340, 11, 5500, 430, 56, 123, 290, 180, 92, '0', '1', '1', '2026-03-16 12:49:23', '192.168.1.91', '2025-04-02 13:20:00', '2026-03-16 12:49:23'),
(92, 'user_92', 'e10adc3949ba59ab', 'user92@example.com', '13800000092', '光明左使', 'http://127.0.0.1:9000/avatar/92.jpg', NULL, '1', '1988-10-10', '投资人，创业者', 'https://vc.com', '北京市海淀区', '创新工场', '投资总监', '发现下一个独角兽', 4560, 18, 12000, 1200, 234, 345, 1200, 560, 345, '0', '1', '1', '2026-03-17 08:55:41', '192.168.1.92', '2024-05-20 09:30:00', '2026-03-17 08:55:41'),
(93, 'user_93', 'e10adc3949ba59ab', 'user93@example.com', '13800000093', '逍遥子', 'http://127.0.0.1:9000/avatar/93.jpg', 'http://127.0.0.1:9000/cover/93_1.jpg', '1', '1992-12-25', '自由职业，数字游民', 'https://nomad.com', '大理市', '无', '旅行作家', '世界那么大，我想去看看', 1670, 8, 3800, 290, 78, 89, 340, 210, 76, '0', '1', '0', '2026-03-15 23:12:08', '192.168.1.93', '2025-07-30 11:45:00', '2026-03-15 23:12:08'),
(94, 'user_94', 'e10adc3949ba59ab', 'user94@example.com', '13800000094', '天山童姥', 'http://127.0.0.1:9000/avatar/94.jpg', 'http://127.0.0.1:9000/cover/94_1.jpg,http://127.0.0.1:9000/cover/94_2.jpg', '2', '1978-06-06', '养生专家，中医师', 'https://tcm.com', '乌鲁木齐市天山区', '中医馆', '主任医师', '治未病', 2980, 13, 7200, 680, 45, 89, 450, 280, 134, '0', '1', '1', '2026-03-16 17:43:29', '192.168.1.94', '2024-08-08 08:15:00', '2026-03-16 17:43:29'),
(95, 'user_95', 'e10adc3949ba59ab', 'user95@example.com', '13800000095', '无崖子', 'http://127.0.0.1:9000/avatar/95.jpg', NULL, '1', '1985-04-04', '大学教授，物理学', 'https://physics.com', '合肥市包河区', '中国科学技术大学', '教授', '探索宇宙奥秘', 3890, 16, 9800, 890, 156, 178, 670, 390, 234, '0', '1', '1', '2026-03-16 10:34:17', '192.168.1.95', '2024-04-15 14:20:00', '2026-03-16 10:34:17'),
(96, 'user_96', 'e10adc3949ba59ab', 'user96@example.com', '13800000096', '李秋水', 'http://127.0.0.1:9000/avatar/96.jpg', 'http://127.0.0.1:9000/cover/96_1.jpg', '2', '1993-11-11', '舞蹈老师，编舞', 'https://dance.com', '昆明市五华区', '舞蹈工作室', '创始人', '用身体表达情感', 1450, 7, 3300, 250, 34, 67, 150, 120, 54, '0', '1', '1', '2026-03-15 20:21:44', '192.168.1.96', '2025-06-28 10:30:00', '2026-03-15 20:21:44'),
(97, 'user_97', 'e10adc3949ba59ab', 'user97@example.com', '13800000097', '扫地僧', 'http://127.0.0.1:9000/avatar/97.jpg', 'http://127.0.0.1:9000/cover/97_1.jpg,http://127.0.0.1:9000/cover/97_2.jpg', '1', '1965-01-01', '图书馆管理员，国学大师', 'https://library.com', '北京市西城区', '国家图书馆', '研究员', '书中自有黄金屋', 5120, 20, 15000, 1500, 89, 123, 890, 450, 267, '0', '1', '1', '2026-03-16 15:56:32', '192.168.1.97', '2024-01-10 09:00:00', '2026-03-16 15:56:32'),
(98, 'user_98', 'e10adc3949ba59ab', 'user98@example.com', '13800000098', '乔峰', 'http://127.0.0.1:9000/avatar/98.jpg', NULL, '1', '1987-10-17', '体育教练，篮球', 'https://basketball.com', '沈阳市和平区', '辽宁男篮', '助理教练', '无兄弟不篮球', 2340, 11, 5600, 470, 67, 89, 290, 180, 87, '0', '1', '1', '2026-03-17 07:18:55', '192.168.1.98', '2024-11-05 16:15:00', '2026-03-17 07:18:55'),
(99, 'user_99', 'e10adc3949ba59ab', 'user99@example.com', '13800000099', '段誉', 'http://127.0.0.1:9000/avatar/99.jpg', 'http://127.0.0.1:9000/cover/99_1.jpg', '1', '1996-05-21', '游戏主播', 'https://twitch.tv', '成都市高新区', '斗鱼直播', '主播', '玩游戏我是认真的', 1780, 8, 4200, 560, 234, 567, 8900, 1200, 89, '0', '1', '1', '2026-03-16 21:43:12', '192.168.1.99', '2025-09-20 20:30:00', '2026-03-16 21:43:12'),
(100, 'user_100', 'e10adc3949ba59ab', 'user100@example.com', '13800000100', '虚竹', 'http://127.0.0.1:9000/avatar/100.jpg', 'http://127.0.0.1:9000/cover/100_1.jpg,http://127.0.0.1:9000/cover/100_2.jpg', '1', '1994-08-08', '围棋老师', 'https://weiqi.com', '杭州市拱墅区', '围棋协会', '职业五段', '棋如人生', 890, 4, 2100, 130, 12, 23, 67, 89, 23, '0', '0', '1', '2026-03-14 13:27:38', '192.168.1.100', '2025-11-12 15:45:00', '2026-03-14 13:27:38');

-- 继续生成测试数据，ID从101到569，只填充必要字段和部分有趣的非空字段
INSERT INTO `user` (`id`, `username`, `password`, `email`, `phone`, `nickname`, `avatar`, `gender`, `status`, `created_at`) VALUES
(101, 'user_101', 'e10adc3949ba59ab', 'user101@example.com', '13800000101', '测试用户101', 'http://127.0.0.1:9000/avatar/101.jpg', '1', '0', '2026-01-15 08:30:00'),
(102, 'user_102', 'e10adc3949ba59ab', 'user102@example.com', '13800000102', '测试用户102', 'http://127.0.0.1:9000/avatar/102.jpg', '2', '0', '2026-01-16 09:45:00'),
(103, 'user_103', 'e10adc3949ba59ab', 'user103@example.com', '13800000103', '测试用户103', NULL, '1', '0', '2026-01-17 10:20:00'),
(104, 'user_104', 'e10adc3949ba59ab', 'user104@example.com', '13800000104', '测试用户104', 'http://127.0.0.1:9000/avatar/104.jpg', '0', '1', '2026-01-18 11:15:00'),
(105, 'user_105', 'e10adc3949ba59ab', 'user105@example.com', '13800000105', '测试用户105', NULL, '2', '0', '2026-01-19 12:30:00'),
(106, 'user_106', 'e10adc3949ba59ab', 'user106@example.com', '13800000106', '测试用户106', 'http://127.0.0.1:9000/avatar/106.jpg', '1', '0', '2026-01-20 13:45:00'),
(107, 'user_107', 'e10adc3949ba59ab', 'user107@example.com', '13800000107', '测试用户107', NULL, '1', '0', '2026-01-21 14:20:00'),
(108, 'user_108', 'e10adc3949ba59ab', 'user108@example.com', '13800000108', '测试用户108', 'http://127.0.0.1:9000/avatar/108.jpg', '2', '0', '2026-01-22 15:10:00'),
(109, 'user_109', 'e10adc3949ba59ab', 'user109@example.com', '13800000109', '测试用户109', NULL, '0', '2', '2026-01-23 16:30:00'),
(110, 'user_110', 'e10adc3949ba59ab', 'user110@example.com', '13800000110', '测试用户110', 'http://127.0.0.1:9000/avatar/110.jpg', '1', '0', '2026-01-24 17:45:00'),
(111, 'user_111', 'e10adc3949ba59ab', 'user111@example.com', '13800000111', '测试用户111', NULL, '2', '0', '2026-01-25 18:20:00'),
(112, 'user_112', 'e10adc3949ba59ab', 'user112@example.com', '13800000112', '测试用户112', 'http://127.0.0.1:9000/avatar/112.jpg', '1', '0', '2026-01-26 19:15:00'),
(113, 'user_113', 'e10adc3949ba59ab', 'user113@example.com', '13800000113', '测试用户113', NULL, '1', '0', '2026-01-27 20:30:00'),
(114, 'user_114', 'e10adc3949ba59ab', 'user114@example.com', '13800000114', '测试用户114', 'http://127.0.0.1:9000/avatar/114.jpg', '2', '0', '2026-01-28 21:45:00'),
(115, 'user_115', 'e10adc3949ba59ab', 'user115@example.com', '13800000115', '测试用户115', NULL, '0', '0', '2026-01-29 22:10:00'),
(116, 'user_116', 'e10adc3949ba59ab', 'user116@example.com', '13800000116', '测试用户116', 'http://127.0.0.1:9000/avatar/116.jpg', '1', '0', '2026-01-30 23:20:00'),
(117, 'user_117', 'e10adc3949ba59ab', 'user117@example.com', '13800000117', '测试用户117', NULL, '2', '1', '2026-02-01 08:30:00'),
(118, 'user_118', 'e10adc3949ba59ab', 'user118@example.com', '13800000118', '测试用户118', 'http://127.0.0.1:9000/avatar/118.jpg', '1', '0', '2026-02-02 09:45:00'),
(119, 'user_119', 'e10adc3949ba59ab', 'user119@example.com', '13800000119', '测试用户119', NULL, '1', '0', '2026-02-03 10:15:00'),
(120, 'user_120', 'e10adc3949ba59ab', 'user120@example.com', '13800000120', '测试用户120', 'http://127.0.0.1:9000/avatar/120.jpg', '2', '0', '2026-02-04 11:30:00'),
(121, 'user_121', 'e10adc3949ba59ab', 'user121@example.com', '13800000121', '测试用户121', NULL, '0', '2', '2026-02-05 12:45:00'),
(122, 'user_122', 'e10adc3949ba59ab', 'user122@example.com', '13800000122', '测试用户122', 'http://127.0.0.1:9000/avatar/122.jpg', '1', '0', '2026-02-06 13:20:00'),
(123, 'user_123', 'e10adc3949ba59ab', 'user123@example.com', '13800000123', '测试用户123', NULL, '2', '0', '2026-02-07 14:10:00'),
(124, 'user_124', 'e10adc3949ba59ab', 'user124@example.com', '13800000124', '测试用户124', 'http://127.0.0.1:9000/avatar/124.jpg', '1', '0', '2026-02-08 15:30:00'),
(125, 'user_125', 'e10adc3949ba59ab', 'user125@example.com', '13800000125', '测试用户125', NULL, '1', '0', '2026-02-09 16:45:00'),
(126, 'user_126', 'e10adc3949ba59ab', 'user126@example.com', '13800000126', '测试用户126', 'http://127.0.0.1:9000/avatar/126.jpg', '2', '0', '2026-02-10 17:20:00'),
(127, 'user_127', 'e10adc3949ba59ab', 'user127@example.com', '13800000127', '测试用户127', NULL, '0', '0', '2026-02-11 18:15:00'),
(128, 'user_128', 'e10adc3949ba59ab', 'user128@example.com', '13800000128', '测试用户128', 'http://127.0.0.1:9000/avatar/128.jpg', '1', '0', '2026-02-12 19:30:00'),
(129, 'user_129', 'e10adc3949ba59ab', 'user129@example.com', '13800000129', '测试用户129', NULL, '2', '1', '2026-02-13 20:45:00'),
(130, 'user_130', 'e10adc3949ba59ab', 'user130@example.com', '13800000130', '测试用户130', 'http://127.0.0.1:9000/avatar/130.jpg', '1', '0', '2026-02-14 21:10:00'),
(131, 'user_131', 'e10adc3949ba59ab', 'user131@example.com', '13800000131', '测试用户131', NULL, '1', '0', '2026-02-15 22:20:00'),
(132, 'user_132', 'e10adc3949ba59ab', 'user132@example.com', '13800000132', '测试用户132', 'http://127.0.0.1:9000/avatar/132.jpg', '2', '0', '2026-02-16 23:30:00'),
(133, 'user_133', 'e10adc3949ba59ab', 'user133@example.com', '13800000133', '测试用户133', NULL, '0', '0', '2026-02-17 08:45:00'),
(134, 'user_134', 'e10adc3949ba59ab', 'user134@example.com', '13800000134', '测试用户134', 'http://127.0.0.1:9000/avatar/134.jpg', '1', '0', '2026-02-18 09:15:00'),
(135, 'user_135', 'e10adc3949ba59ab', 'user135@example.com', '13800000135', '测试用户135', NULL, '2', '0', '2026-02-19 10:30:00'),
(136, 'user_136', 'e10adc3949ba59ab', 'user136@example.com', '13800000136', '测试用户136', 'http://127.0.0.1:9000/avatar/136.jpg', '1', '0', '2026-02-20 11:45:00'),
(137, 'user_137', 'e10adc3949ba59ab', 'user137@example.com', '13800000137', '测试用户137', NULL, '1', '2', '2026-02-21 12:20:00'),
(138, 'user_138', 'e10adc3949ba59ab', 'user138@example.com', '13800000138', '测试用户138', 'http://127.0.0.1:9000/avatar/138.jpg', '2', '0', '2026-02-22 13:10:00'),
(139, 'user_139', 'e10adc3949ba59ab', 'user139@example.com', '13800000139', '测试用户139', NULL, '0', '0', '2026-02-23 14:30:00'),
(140, 'user_140', 'e10adc3949ba59ab', 'user140@example.com', '13800000140', '测试用户140', 'http://127.0.0.1:9000/avatar/140.jpg', '1', '0', '2026-02-24 15:45:00'),
(141, 'user_141', 'e10adc3949ba59ab', 'user141@example.com', '13800000141', '测试用户141', NULL, '2', '0', '2026-02-25 16:20:00'),
(142, 'user_142', 'e10adc3949ba59ab', 'user142@example.com', '13800000142', '测试用户142', 'http://127.0.0.1:9000/avatar/142.jpg', '1', '0', '2026-02-26 17:15:00'),
(143, 'user_143', 'e10adc3949ba59ab', 'user143@example.com', '13800000143', '测试用户143', NULL, '1', '0', '2026-02-27 18:30:00'),
(144, 'user_144', 'e10adc3949ba59ab', 'user144@example.com', '13800000144', '测试用户144', 'http://127.0.0.1:9000/avatar/144.jpg', '2', '0', '2026-02-28 19:45:00'),
(145, 'user_145', 'e10adc3949ba59ab', 'user145@example.com', '13800000145', '测试用户145', NULL, '0', '1', '2026-03-01 20:10:00'),
(146, 'user_146', 'e10adc3949ba59ab', 'user146@example.com', '13800000146', '测试用户146', 'http://127.0.0.1:9000/avatar/146.jpg', '1', '0', '2026-03-02 21:20:00'),
(147, 'user_147', 'e10adc3949ba59ab', 'user147@example.com', '13800000147', '测试用户147', NULL, '2', '0', '2026-03-03 22:30:00'),
(148, 'user_148', 'e10adc3949ba59ab', 'user148@example.com', '13800000148', '测试用户148', 'http://127.0.0.1:9000/avatar/148.jpg', '1', '0', '2026-03-04 23:45:00'),
(149, 'user_149', 'e10adc3949ba59ab', 'user149@example.com', '13800000149', '测试用户149', NULL, '1', '0', '2026-03-05 08:15:00'),
(150, 'user_150', 'e10adc3949ba59ab', 'user150@example.com', '13800000150', '测试用户150', 'http://127.0.0.1:9000/avatar/150.jpg', '2', '0', '2026-03-06 09:30:00'),
(151, 'user_151', 'e10adc3949ba59ab', 'user151@example.com', '13800000151', '测试用户151', NULL, '0', '0', '2026-03-07 10:45:00'),
(152, 'user_152', 'e10adc3949ba59ab', 'user152@example.com', '13800000152', '测试用户152', 'http://127.0.0.1:9000/avatar/152.jpg', '1', '0', '2026-03-08 11:20:00'),
(153, 'user_153', 'e10adc3949ba59ab', 'user153@example.com', '13800000153', '测试用户153', NULL, '2', '2', '2026-03-09 12:10:00'),
(154, 'user_154', 'e10adc3949ba59ab', 'user154@example.com', '13800000154', '测试用户154', 'http://127.0.0.1:9000/avatar/154.jpg', '1', '0', '2026-03-10 13:30:00'),
(155, 'user_155', 'e10adc3949ba59ab', 'user155@example.com', '13800000155', '测试用户155', NULL, '1', '0', '2026-03-11 14:45:00'),
(156, 'user_156', 'e10adc3949ba59ab', 'user156@example.com', '13800000156', '测试用户156', 'http://127.0.0.1:9000/avatar/156.jpg', '2', '0', '2026-03-12 15:20:00'),
(157, 'user_157', 'e10adc3949ba59ab', 'user157@example.com', '13800000157', '测试用户157', NULL, '0', '0', '2026-03-13 16:15:00'),
(158, 'user_158', 'e10adc3949ba59ab', 'user158@example.com', '13800000158', '测试用户158', 'http://127.0.0.1:9000/avatar/158.jpg', '1', '0', '2026-03-14 17:30:00'),
(159, 'user_159', 'e10adc3949ba59ab', 'user159@example.com', '13800000159', '测试用户159', NULL, '2', '1', '2026-03-15 18:45:00'),
(160, 'user_160', 'e10adc3949ba59ab', 'user160@example.com', '13800000160', '测试用户160', 'http://127.0.0.1:9000/avatar/160.jpg', '1', '0', '2026-03-16 19:10:00'),
(161, 'user_161', 'e10adc3949ba59ab', 'user161@example.com', '13800000161', '测试用户161', NULL, '1', '0', '2026-03-17 20:20:00'),
(162, 'user_162', 'e10adc3949ba59ab', 'user162@example.com', '13800000162', '测试用户162', 'http://127.0.0.1:9000/avatar/162.jpg', '2', '0', '2026-03-18 21:30:00'),
(163, 'user_163', 'e10adc3949ba59ab', 'user163@example.com', '13800000163', '测试用户163', NULL, '0', '0', '2026-03-19 22:45:00'),
(164, 'user_164', 'e10adc3949ba59ab', 'user164@example.com', '13800000164', '测试用户164', 'http://127.0.0.1:9000/avatar/164.jpg', '1', '0', '2026-03-20 23:15:00'),
(165, 'user_165', 'e10adc3949ba59ab', 'user165@example.com', '13800000165', '测试用户165', NULL, '2', '0', '2026-03-21 08:30:00'),
(166, 'user_166', 'e10adc3949ba59ab', 'user166@example.com', '13800000166', '测试用户166', 'http://127.0.0.1:9000/avatar/166.jpg', '1', '0', '2026-03-22 09:45:00'),
(167, 'user_167', 'e10adc3949ba59ab', 'user167@example.com', '13800000167', '测试用户167', NULL, '1', '2', '2026-03-23 10:20:00'),
(168, 'user_168', 'e10adc3949ba59ab', 'user168@example.com', '13800000168', '测试用户168', 'http://127.0.0.1:9000/avatar/168.jpg', '2', '0', '2026-03-24 11:10:00'),
(169, 'user_169', 'e10adc3949ba59ab', 'user169@example.com', '13800000169', '测试用户169', NULL, '0', '0', '2026-03-25 12:30:00'),
(170, 'user_170', 'e10adc3949ba59ab', 'user170@example.com', '13800000170', '测试用户170', 'http://127.0.0.1:9000/avatar/170.jpg', '1', '0', '2026-03-26 13:45:00'),
(171, 'user_171', 'e10adc3949ba59ab', 'user171@example.com', '13800000171', '测试用户171', NULL, '2', '0', '2026-03-27 14:20:00'),
(172, 'user_172', 'e10adc3949ba59ab', 'user172@example.com', '13800000172', '测试用户172', 'http://127.0.0.1:9000/avatar/172.jpg', '1', '0', '2026-03-28 15:15:00'),
(173, 'user_173', 'e10adc3949ba59ab', 'user173@example.com', '13800000173', '测试用户173', NULL, '1', '0', '2026-03-29 16:30:00'),
(174, 'user_174', 'e10adc3949ba59ab', 'user174@example.com', '13800000174', '测试用户174', 'http://127.0.0.1:9000/avatar/174.jpg', '2', '0', '2026-03-30 17:45:00'),
(175, 'user_175', 'e10adc3949ba59ab', 'user175@example.com', '13800000175', '测试用户175', NULL, '0', '1', '2026-03-31 18:10:00'),
(176, 'user_176', 'e10adc3949ba59ab', 'user176@example.com', '13800000176', '测试用户176', 'http://127.0.0.1:9000/avatar/176.jpg', '1', '0', '2026-04-01 19:20:00'),
(177, 'user_177', 'e10adc3949ba59ab', 'user177@example.com', '13800000177', '测试用户177', NULL, '2', '0', '2026-04-02 20:30:00'),
(178, 'user_178', 'e10adc3949ba59ab', 'user178@example.com', '13800000178', '测试用户178', 'http://127.0.0.1:9000/avatar/178.jpg', '1', '0', '2026-04-03 21:45:00'),
(179, 'user_179', 'e10adc3949ba59ab', 'user179@example.com', '13800000179', '测试用户179', NULL, '1', '0', '2026-04-04 22:15:00'),
(180, 'user_180', 'e10adc3949ba59ab', 'user180@example.com', '13800000180', '测试用户180', 'http://127.0.0.1:9000/avatar/180.jpg', '2', '0', '2026-04-05 23:30:00'),
(181, 'user_181', 'e10adc3949ba59ab', 'user181@example.com', '13800000181', '测试用户181', NULL, '0', '0', '2026-04-06 08:45:00'),
(182, 'user_182', 'e10adc3949ba59ab', 'user182@example.com', '13800000182', '测试用户182', 'http://127.0.0.1:9000/avatar/182.jpg', '1', '0', '2026-04-07 09:20:00'),
(183, 'user_183', 'e10adc3949ba59ab', 'user183@example.com', '13800000183', '测试用户183', NULL, '2', '2', '2026-04-08 10:10:00'),
(184, 'user_184', 'e10adc3949ba59ab', 'user184@example.com', '13800000184', '测试用户184', 'http://127.0.0.1:9000/avatar/184.jpg', '1', '0', '2026-04-09 11:30:00'),
(185, 'user_185', 'e10adc3949ba59ab', 'user185@example.com', '13800000185', '测试用户185', NULL, '1', '0', '2026-04-10 12:45:00'),
(186, 'user_186', 'e10adc3949ba59ab', 'user186@example.com', '13800000186', '测试用户186', 'http://127.0.0.1:9000/avatar/186.jpg', '2', '0', '2026-04-11 13:20:00'),
(187, 'user_187', 'e10adc3949ba59ab', 'user187@example.com', '13800000187', '测试用户187', NULL, '0', '0', '2026-04-12 14:15:00'),
(188, 'user_188', 'e10adc3949ba59ab', 'user188@example.com', '13800000188', '测试用户188', 'http://127.0.0.1:9000/avatar/188.jpg', '1', '0', '2026-04-13 15:30:00'),
(189, 'user_189', 'e10adc3949ba59ab', 'user189@example.com', '13800000189', '测试用户189', NULL, '2', '0', '2026-04-14 16:45:00'),
(190, 'user_190', 'e10adc3949ba59ab', 'user190@example.com', '13800000190', '测试用户190', 'http://127.0.0.1:9000/avatar/190.jpg', '1', '1', '2026-04-15 17:10:00'),
(191, 'user_191', 'e10adc3949ba59ab', 'user191@example.com', '13800000191', '测试用户191', NULL, '1', '0', '2026-04-16 18:20:00'),
(192, 'user_192', 'e10adc3949ba59ab', 'user192@example.com', '13800000192', '测试用户192', 'http://127.0.0.1:9000/avatar/192.jpg', '2', '0', '2026-04-17 19:30:00'),
(193, 'user_193', 'e10adc3949ba59ab', 'user193@example.com', '13800000193', '测试用户193', NULL, '0', '0', '2026-04-18 20:45:00'),
(194, 'user_194', 'e10adc3949ba59ab', 'user194@example.com', '13800000194', '测试用户194', 'http://127.0.0.1:9000/avatar/194.jpg', '1', '0', '2026-04-19 21:15:00'),
(195, 'user_195', 'e10adc3949ba59ab', 'user195@example.com', '13800000195', '测试用户195', NULL, '2', '0', '2026-04-20 22:30:00'),
(196, 'user_196', 'e10adc3949ba59ab', 'user196@example.com', '13800000196', '测试用户196', 'http://127.0.0.1:9000/avatar/196.jpg', '1', '0', '2026-04-21 23:45:00'),
(197, 'user_197', 'e10adc3949ba59ab', 'user197@example.com', '13800000197', '测试用户197', NULL, '1', '2', '2026-04-22 08:20:00'),
(198, 'user_198', 'e10adc3949ba59ab', 'user198@example.com', '13800000198', '测试用户198', 'http://127.0.0.1:9000/avatar/198.jpg', '2', '0', '2026-04-23 09:10:00'),
(199, 'user_199', 'e10adc3949ba59ab', 'user199@example.com', '13800000199', '测试用户199', NULL, '0', '0', '2026-04-24 10:30:00'),
(200, 'user_200', 'e10adc3949ba59ab', 'user200@example.com', '13800000200', '测试用户200', 'http://127.0.0.1:9000/avatar/200.jpg', '1', '0', '2026-04-25 11:45:00');

-- 继续生成测试数据，ID从201到350
INSERT INTO `user` (`id`, `username`, `password`, `email`, `phone`, `nickname`, `avatar`, `gender`, `status`, `created_at`) VALUES
(201, 'user_201', 'e10adc3949ba59ab', 'user201@example.com', '13800000201', '测试用户201', 'http://127.0.0.1:9000/avatar/201.jpg', '1', '0', '2026-04-26 12:20:00'),
(202, 'user_202', 'e10adc3949ba59ab', 'user202@example.com', '13800000202', '测试用户202', NULL, '2', '0', '2026-04-27 13:15:00'),
(203, 'user_203', 'e10adc3949ba59ab', 'user203@example.com', '13800000203', '测试用户203', 'http://127.0.0.1:9000/avatar/203.jpg', '0', '0', '2026-04-28 14:30:00'),
(204, 'user_204', 'e10adc3949ba59ab', 'user204@example.com', '13800000204', '测试用户204', NULL, '1', '1', '2026-04-29 15:45:00'),
(205, 'user_205', 'e10adc3949ba59ab', 'user205@example.com', '13800000205', '测试用户205', 'http://127.0.0.1:9000/avatar/205.jpg', '2', '0', '2026-04-30 16:20:00'),
(206, 'user_206', 'e10adc3949ba59ab', 'user206@example.com', '13800000206', '测试用户206', NULL, '1', '0', '2026-05-01 17:10:00'),
(207, 'user_207', 'e10adc3949ba59ab', 'user207@example.com', '13800000207', '测试用户207', 'http://127.0.0.1:9000/avatar/207.jpg', '1', '0', '2026-05-02 18:30:00'),
(208, 'user_208', 'e10adc3949ba59ab', 'user208@example.com', '13800000208', '测试用户208', NULL, '2', '0', '2026-05-03 19:45:00'),
(209, 'user_209', 'e10adc3949ba59ab', 'user209@example.com', '13800000209', '测试用户209', 'http://127.0.0.1:9000/avatar/209.jpg', '0', '2', '2026-05-04 20:15:00'),
(210, 'user_210', 'e10adc3949ba59ab', 'user210@example.com', '13800000210', '测试用户210', NULL, '1', '0', '2026-05-05 21:30:00'),
(211, 'user_211', 'e10adc3949ba59ab', 'user211@example.com', '13800000211', '测试用户211', 'http://127.0.0.1:9000/avatar/211.jpg', '2', '0', '2026-05-06 22:45:00'),
(212, 'user_212', 'e10adc3949ba59ab', 'user212@example.com', '13800000212', '测试用户212', NULL, '1', '0', '2026-05-07 23:20:00'),
(213, 'user_213', 'e10adc3949ba59ab', 'user213@example.com', '13800000213', '测试用户213', 'http://127.0.0.1:9000/avatar/213.jpg', '1', '0', '2026-05-08 08:10:00'),
(214, 'user_214', 'e10adc3949ba59ab', 'user214@example.com', '13800000214', '测试用户214', NULL, '2', '0', '2026-05-09 09:30:00'),
(215, 'user_215', 'e10adc3949ba59ab', 'user215@example.com', '13800000215', '测试用户215', 'http://127.0.0.1:9000/avatar/215.jpg', '0', '0', '2026-05-10 10:45:00'),
(216, 'user_216', 'e10adc3949ba59ab', 'user216@example.com', '13800000216', '测试用户216', NULL, '1', '1', '2026-05-11 11:20:00'),
(217, 'user_217', 'e10adc3949ba59ab', 'user217@example.com', '13800000217', '测试用户217', 'http://127.0.0.1:9000/avatar/217.jpg', '2', '0', '2026-05-12 12:15:00'),
(218, 'user_218', 'e10adc3949ba59ab', 'user218@example.com', '13800000218', '测试用户218', NULL, '1', '0', '2026-05-13 13:30:00'),
(219, 'user_219', 'e10adc3949ba59ab', 'user219@example.com', '13800000219', '测试用户219', 'http://127.0.0.1:9000/avatar/219.jpg', '1', '0', '2026-05-14 14:45:00'),
(220, 'user_220', 'e10adc3949ba59ab', 'user220@example.com', '13800000220', '测试用户220', NULL, '2', '0', '2026-05-15 15:20:00'),
(221, 'user_221', 'e10adc3949ba59ab', 'user221@example.com', '13800000221', '测试用户221', 'http://127.0.0.1:9000/avatar/221.jpg', '0', '0', '2026-05-16 16:10:00'),
(222, 'user_222', 'e10adc3949ba59ab', 'user222@example.com', '13800000222', '测试用户222', NULL, '1', '2', '2026-05-17 17:30:00'),
(223, 'user_223', 'e10adc3949ba59ab', 'user223@example.com', '13800000223', '测试用户223', 'http://127.0.0.1:9000/avatar/223.jpg', '2', '0', '2026-05-18 18:45:00'),
(224, 'user_224', 'e10adc3949ba59ab', 'user224@example.com', '13800000224', '测试用户224', NULL, '1', '0', '2026-05-19 19:15:00'),
(225, 'user_225', 'e10adc3949ba59ab', 'user225@example.com', '13800000225', '测试用户225', 'http://127.0.0.1:9000/avatar/225.jpg', '1', '0', '2026-05-20 20:30:00'),
(226, 'user_226', 'e10adc3949ba59ab', 'user226@example.com', '13800000226', '测试用户226', NULL, '2', '0', '2026-05-21 21:45:00'),
(227, 'user_227', 'e10adc3949ba59ab', 'user227@example.com', '13800000227', '测试用户227', 'http://127.0.0.1:9000/avatar/227.jpg', '0', '0', '2026-05-22 22:20:00'),
(228, 'user_228', 'e10adc3949ba59ab', 'user228@example.com', '13800000228', '测试用户228', NULL, '1', '0', '2026-05-23 23:10:00'),
(229, 'user_229', 'e10adc3949ba59ab', 'user229@example.com', '13800000229', '测试用户229', 'http://127.0.0.1:9000/avatar/229.jpg', '2', '0', '2026-05-24 08:30:00'),
(230, 'user_230', 'e10adc3949ba59ab', 'user230@example.com', '13800000230', '测试用户230', NULL, '1', '1', '2026-05-25 09:45:00'),
(231, 'user_231', 'e10adc3949ba59ab', 'user231@example.com', '13800000231', '测试用户231', 'http://127.0.0.1:9000/avatar/231.jpg', '1', '0', '2026-05-26 10:20:00'),
(232, 'user_232', 'e10adc3949ba59ab', 'user232@example.com', '13800000232', '测试用户232', NULL, '2', '0', '2026-05-27 11:15:00'),
(233, 'user_233', 'e10adc3949ba59ab', 'user233@example.com', '13800000233', '测试用户233', 'http://127.0.0.1:9000/avatar/233.jpg', '0', '0', '2026-05-28 12:30:00'),
(234, 'user_234', 'e10adc3949ba59ab', 'user234@example.com', '13800000234', '测试用户234', NULL, '1', '0', '2026-05-29 13:45:00'),
(235, 'user_235', 'e10adc3949ba59ab', 'user235@example.com', '13800000235', '测试用户235', 'http://127.0.0.1:9000/avatar/235.jpg', '2', '2', '2026-05-30 14:20:00'),
(236, 'user_236', 'e10adc3949ba59ab', 'user236@example.com', '13800000236', '测试用户236', NULL, '1', '0', '2026-05-31 15:10:00'),
(237, 'user_237', 'e10adc3949ba59ab', 'user237@example.com', '13800000237', '测试用户237', 'http://127.0.0.1:9000/avatar/237.jpg', '1', '0', '2026-06-01 16:30:00'),
(238, 'user_238', 'e10adc3949ba59ab', 'user238@example.com', '13800000238', '测试用户238', NULL, '2', '0', '2026-06-02 17:45:00'),
(239, 'user_239', 'e10adc3949ba59ab', 'user239@example.com', '13800000239', '测试用户239', 'http://127.0.0.1:9000/avatar/239.jpg', '0', '0', '2026-06-03 18:15:00'),
(240, 'user_240', 'e10adc3949ba59ab', 'user240@example.com', '13800000240', '测试用户240', NULL, '1', '0', '2026-06-04 19:30:00'),
(241, 'user_241', 'e10adc3949ba59ab', 'user241@example.com', '13800000241', '测试用户241', 'http://127.0.0.1:9000/avatar/241.jpg', '2', '0', '2026-06-05 20:45:00'),
(242, 'user_242', 'e10adc3949ba59ab', 'user242@example.com', '13800000242', '测试用户242', NULL, '1', '0', '2026-06-06 21:20:00'),
(243, 'user_243', 'e10adc3949ba59ab', 'user243@example.com', '13800000243', '测试用户243', 'http://127.0.0.1:9000/avatar/243.jpg', '1', '1', '2026-06-07 22:10:00'),
(244, 'user_244', 'e10adc3949ba59ab', 'user244@example.com', '13800000244', '测试用户244', NULL, '2', '0', '2026-06-08 23:30:00'),
(245, 'user_245', 'e10adc3949ba59ab', 'user245@example.com', '13800000245', '测试用户245', 'http://127.0.0.1:9000/avatar/245.jpg', '0', '0', '2026-06-09 08:45:00'),
(246, 'user_246', 'e10adc3949ba59ab', 'user246@example.com', '13800000246', '测试用户246', NULL, '1', '0', '2026-06-10 09:20:00'),
(247, 'user_247', 'e10adc3949ba59ab', 'user247@example.com', '13800000247', '测试用户247', 'http://127.0.0.1:9000/avatar/247.jpg', '2', '0', '2026-06-11 10:15:00'),
(248, 'user_248', 'e10adc3949ba59ab', 'user248@example.com', '13800000248', '测试用户248', NULL, '1', '0', '2026-06-12 11:30:00'),
(249, 'user_249', 'e10adc3949ba59ab', 'user249@example.com', '13800000249', '测试用户249', 'http://127.0.0.1:9000/avatar/249.jpg', '1', '2', '2026-06-13 12:45:00'),
(250, 'user_250', 'e10adc3949ba59ab', 'user250@example.com', '13800000250', '测试用户250', NULL, '2', '0', '2026-06-14 13:20:00'),
(251, 'user_251', 'e10adc3949ba59ab', 'user251@example.com', '13800000251', '测试用户251', 'http://127.0.0.1:9000/avatar/251.jpg', '0', '0', '2026-06-15 14:10:00'),
(252, 'user_252', 'e10adc3949ba59ab', 'user252@example.com', '13800000252', '测试用户252', NULL, '1', '0', '2026-06-16 15:30:00'),
(253, 'user_253', 'e10adc3949ba59ab', 'user253@example.com', '13800000253', '测试用户253', 'http://127.0.0.1:9000/avatar/253.jpg', '2', '0', '2026-06-17 16:45:00'),
(254, 'user_254', 'e10adc3949ba59ab', 'user254@example.com', '13800000254', '测试用户254', NULL, '1', '0', '2026-06-18 17:15:00'),
(255, 'user_255', 'e10adc3949ba59ab', 'user255@example.com', '13800000255', '测试用户255', 'http://127.0.0.1:9000/avatar/255.jpg', '1', '1', '2026-06-19 18:30:00'),
(256, 'user_256', 'e10adc3949ba59ab', 'user256@example.com', '13800000256', '测试用户256', NULL, '2', '0', '2026-06-20 19:45:00'),
(257, 'user_257', 'e10adc3949ba59ab', 'user257@example.com', '13800000257', '测试用户257', 'http://127.0.0.1:9000/avatar/257.jpg', '0', '0', '2026-06-21 20:20:00'),
(258, 'user_258', 'e10adc3949ba59ab', 'user258@example.com', '13800000258', '测试用户258', NULL, '1', '0', '2026-06-22 21:10:00'),
(259, 'user_259', 'e10adc3949ba59ab', 'user259@example.com', '13800000259', '测试用户259', 'http://127.0.0.1:9000/avatar/259.jpg', '2', '0', '2026-06-23 22:30:00'),
(260, 'user_260', 'e10adc3949ba59ab', 'user260@example.com', '13800000260', '测试用户260', NULL, '1', '0', '2026-06-24 23:45:00'),
(261, 'user_261', 'e10adc3949ba59ab', 'user261@example.com', '13800000261', '测试用户261', 'http://127.0.0.1:9000/avatar/261.jpg', '1', '0', '2026-06-25 08:20:00'),
(262, 'user_262', 'e10adc3949ba59ab', 'user262@example.com', '13800000262', '测试用户262', NULL, '2', '2', '2026-06-26 09:15:00'),
(263, 'user_263', 'e10adc3949ba59ab', 'user263@example.com', '13800000263', '测试用户263', 'http://127.0.0.1:9000/avatar/263.jpg', '0', '0', '2026-06-27 10:30:00'),
(264, 'user_264', 'e10adc3949ba59ab', 'user264@example.com', '13800000264', '测试用户264', NULL, '1', '0', '2026-06-28 11:45:00'),
(265, 'user_265', 'e10adc3949ba59ab', 'user265@example.com', '13800000265', '测试用户265', 'http://127.0.0.1:9000/avatar/265.jpg', '2', '0', '2026-06-29 12:20:00'),
(266, 'user_266', 'e10adc3949ba59ab', 'user266@example.com', '13800000266', '测试用户266', NULL, '1', '0', '2026-06-30 13:10:00'),
(267, 'user_267', 'e10adc3949ba59ab', 'user267@example.com', '13800000267', '测试用户267', 'http://127.0.0.1:9000/avatar/267.jpg', '1', '0', '2026-07-01 14:30:00'),
(268, 'user_268', 'e10adc3949ba59ab', 'user268@example.com', '13800000268', '测试用户268', NULL, '2', '1', '2026-07-02 15:45:00'),
(269, 'user_269', 'e10adc3949ba59ab', 'user269@example.com', '13800000269', '测试用户269', 'http://127.0.0.1:9000/avatar/269.jpg', '0', '0', '2026-07-03 16:20:00'),
(270, 'user_270', 'e10adc3949ba59ab', 'user270@example.com', '13800000270', '测试用户270', NULL, '1', '0', '2026-07-04 17:15:00'),
(271, 'user_271', 'e10adc3949ba59ab', 'user271@example.com', '13800000271', '测试用户271', 'http://127.0.0.1:9000/avatar/271.jpg', '2', '0', '2026-07-05 18:30:00'),
(272, 'user_272', 'e10adc3949ba59ab', 'user272@example.com', '13800000272', '测试用户272', NULL, '1', '0', '2026-07-06 19:45:00'),
(273, 'user_273', 'e10adc3949ba59ab', 'user273@example.com', '13800000273', '测试用户273', 'http://127.0.0.1:9000/avatar/273.jpg', '1', '0', '2026-07-07 20:10:00'),
(274, 'user_274', 'e10adc3949ba59ab', 'user274@example.com', '13800000274', '测试用户274', NULL, '2', '2', '2026-07-08 21:20:00'),
(275, 'user_275', 'e10adc3949ba59ab', 'user275@example.com', '13800000275', '测试用户275', 'http://127.0.0.1:9000/avatar/275.jpg', '0', '0', '2026-07-09 22:30:00'),
(276, 'user_276', 'e10adc3949ba59ab', 'user276@example.com', '13800000276', '测试用户276', NULL, '1', '0', '2026-07-10 23:45:00'),
(277, 'user_277', 'e10adc3949ba59ab', 'user277@example.com', '13800000277', '测试用户277', 'http://127.0.0.1:9000/avatar/277.jpg', '2', '0', '2026-07-11 08:15:00'),
(278, 'user_278', 'e10adc3949ba59ab', 'user278@example.com', '13800000278', '测试用户278', NULL, '1', '0', '2026-07-12 09:30:00'),
(279, 'user_279', 'e10adc3949ba59ab', 'user279@example.com', '13800000279', '测试用户279', 'http://127.0.0.1:9000/avatar/279.jpg', '1', '0', '2026-07-13 10:45:00'),
(280, 'user_280', 'e10adc3949ba59ab', 'user280@example.com', '13800000280', '测试用户280', NULL, '2', '1', '2026-07-14 11:20:00'),
(281, 'user_281', 'e10adc3949ba59ab', 'user281@example.com', '13800000281', '测试用户281', 'http://127.0.0.1:9000/avatar/281.jpg', '0', '0', '2026-07-15 12:10:00'),
(282, 'user_282', 'e10adc3949ba59ab', 'user282@example.com', '13800000282', '测试用户282', NULL, '1', '0', '2026-07-16 13:30:00'),
(283, 'user_283', 'e10adc3949ba59ab', 'user283@example.com', '13800000283', '测试用户283', 'http://127.0.0.1:9000/avatar/283.jpg', '2', '0', '2026-07-17 14:45:00'),
(284, 'user_284', 'e10adc3949ba59ab', 'user284@example.com', '13800000284', '测试用户284', NULL, '1', '0', '2026-07-18 15:20:00'),
(285, 'user_285', 'e10adc3949ba59ab', 'user285@example.com', '13800000285', '测试用户285', 'http://127.0.0.1:9000/avatar/285.jpg', '1', '0', '2026-07-19 16:15:00'),
(286, 'user_286', 'e10adc3949ba59ab', 'user286@example.com', '13800000286', '测试用户286', NULL, '2', '2', '2026-07-20 17:30:00'),
(287, 'user_287', 'e10adc3949ba59ab', 'user287@example.com', '13800000287', '测试用户287', 'http://127.0.0.1:9000/avatar/287.jpg', '0', '0', '2026-07-21 18:45:00'),
(288, 'user_288', 'e10adc3949ba59ab', 'user288@example.com', '13800000288', '测试用户288', NULL, '1', '0', '2026-07-22 19:10:00'),
(289, 'user_289', 'e10adc3949ba59ab', 'user289@example.com', '13800000289', '测试用户289', 'http://127.0.0.1:9000/avatar/289.jpg', '2', '0', '2026-07-23 20:20:00'),
(290, 'user_290', 'e10adc3949ba59ab', 'user290@example.com', '13800000290', '测试用户290', NULL, '1', '0', '2026-07-24 21:30:00'),
(291, 'user_291', 'e10adc3949ba59ab', 'user291@example.com', '13800000291', '测试用户291', 'http://127.0.0.1:9000/avatar/291.jpg', '1', '0', '2026-07-25 22:45:00'),
(292, 'user_292', 'e10adc3949ba59ab', 'user292@example.com', '13800000292', '测试用户292', NULL, '2', '1', '2026-07-26 23:15:00'),
(293, 'user_293', 'e10adc3949ba59ab', 'user293@example.com', '13800000293', '测试用户293', 'http://127.0.0.1:9000/avatar/293.jpg', '0', '0', '2026-07-27 08:30:00'),
(294, 'user_294', 'e10adc3949ba59ab', 'user294@example.com', '13800000294', '测试用户294', NULL, '1', '0', '2026-07-28 09:45:00'),
(295, 'user_295', 'e10adc3949ba59ab', 'user295@example.com', '13800000295', '测试用户295', 'http://127.0.0.1:9000/avatar/295.jpg', '2', '0', '2026-07-29 10:20:00'),
(296, 'user_296', 'e10adc3949ba59ab', 'user296@example.com', '13800000296', '测试用户296', NULL, '1', '0', '2026-07-30 11:10:00'),
(297, 'user_297', 'e10adc3949ba59ab', 'user297@example.com', '13800000297', '测试用户297', 'http://127.0.0.1:9000/avatar/297.jpg', '1', '0', '2026-07-31 12:30:00'),
(298, 'user_298', 'e10adc3949ba59ab', 'user298@example.com', '13800000298', '测试用户298', NULL, '2', '2', '2026-08-01 13:45:00'),
(299, 'user_299', 'e10adc3949ba59ab', 'user299@example.com', '13800000299', '测试用户299', 'http://127.0.0.1:9000/avatar/299.jpg', '0', '0', '2026-08-02 14:20:00'),
(300, 'user_300', 'e10adc3949ba59ab', 'user300@example.com', '13800000300', '测试用户300', NULL, '1', '0', '2026-08-03 15:15:00'),
(301, 'user_301', 'e10adc3949ba59ab', 'user301@example.com', '13800000301', '测试用户301', 'http://127.0.0.1:9000/avatar/301.jpg', '2', '0', '2026-08-04 16:30:00'),
(302, 'user_302', 'e10adc3949ba59ab', 'user302@example.com', '13800000302', '测试用户302', NULL, '1', '0', '2026-08-05 17:45:00'),
(303, 'user_303', 'e10adc3949ba59ab', 'user303@example.com', '13800000303', '测试用户303', 'http://127.0.0.1:9000/avatar/303.jpg', '1', '0', '2026-08-06 18:10:00'),
(304, 'user_304', 'e10adc3949ba59ab', 'user304@example.com', '13800000304', '测试用户304', NULL, '2', '1', '2026-08-07 19:20:00'),
(305, 'user_305', 'e10adc3949ba59ab', 'user305@example.com', '13800000305', '测试用户305', 'http://127.0.0.1:9000/avatar/305.jpg', '0', '0', '2026-08-08 20:30:00'),
(306, 'user_306', 'e10adc3949ba59ab', 'user306@example.com', '13800000306', '测试用户306', NULL, '1', '0', '2026-08-09 21:45:00'),
(307, 'user_307', 'e10adc3949ba59ab', 'user307@example.com', '13800000307', '测试用户307', 'http://127.0.0.1:9000/avatar/307.jpg', '2', '0', '2026-08-10 22:15:00'),
(308, 'user_308', 'e10adc3949ba59ab', 'user308@example.com', '13800000308', '测试用户308', NULL, '1', '0', '2026-08-11 23:30:00'),
(309, 'user_309', 'e10adc3949ba59ab', 'user309@example.com', '13800000309', '测试用户309', 'http://127.0.0.1:9000/avatar/309.jpg', '1', '0', '2026-08-12 08:45:00'),
(310, 'user_310', 'e10adc3949ba59ab', 'user310@example.com', '13800000310', '测试用户310', NULL, '2', '0', '2026-08-13 09:20:00'),
(311, 'user_311', 'e10adc3949ba59ab', 'user311@example.com', '13800000311', '测试用户311', 'http://127.0.0.1:9000/avatar/311.jpg', '0', '2', '2026-08-14 10:10:00'),
(312, 'user_312', 'e10adc3949ba59ab', 'user312@example.com', '13800000312', '测试用户312', NULL, '1', '0', '2026-08-15 11:30:00'),
(313, 'user_313', 'e10adc3949ba59ab', 'user313@example.com', '13800000313', '测试用户313', 'http://127.0.0.1:9000/avatar/313.jpg', '2', '0', '2026-08-16 12:45:00'),
(314, 'user_314', 'e10adc3949ba59ab', 'user314@example.com', '13800000314', '测试用户314', NULL, '1', '0', '2026-08-17 13:20:00'),
(315, 'user_315', 'e10adc3949ba59ab', 'user315@example.com', '13800000315', '测试用户315', 'http://127.0.0.1:9000/avatar/315.jpg', '1', '0', '2026-08-18 14:15:00'),
(316, 'user_316', 'e10adc3949ba59ab', 'user316@example.com', '13800000316', '测试用户316', NULL, '2', '0', '2026-08-19 15:30:00'),
(317, 'user_317', 'e10adc3949ba59ab', 'user317@example.com', '13800000317', '测试用户317', 'http://127.0.0.1:9000/avatar/317.jpg', '0', '0', '2026-08-20 16:45:00'),
(318, 'user_318', 'e10adc3949ba59ab', 'user318@example.com', '13800000318', '测试用户318', NULL, '1', '1', '2026-08-21 17:10:00'),
(319, 'user_319', 'e10adc3949ba59ab', 'user319@example.com', '13800000319', '测试用户319', 'http://127.0.0.1:9000/avatar/319.jpg', '2', '0', '2026-08-22 18:20:00'),
(320, 'user_320', 'e10adc3949ba59ab', 'user320@example.com', '13800000320', '测试用户320', NULL, '1', '0', '2026-08-23 19:30:00')


INSERT INTO user_role (user_id, role_id) VALUES
-- 用户1 test
(1, 2),
-- 用户2 admin
(2, 1),
-- 用户3 superuser
(3, 1),
-- 用户4 sysadmin
(4, 1),
-- 用户5 webmaster
(5, 2), (5, 6),
-- 用户6 moderator
(6, 2), (6, 4),
-- 用户7 coder_li
(7, 2), (7, 5),
-- 用户8 frontend_wang
(8, 2), (8, 5),
-- 用户9 python_zhang
(9, 2), (9, 5),
-- 用户10 go_dev
(10, 2), (10, 5),
-- 用户11 mobile_zhao
(11, 2), (11, 5),
-- 用户12 db_admin（增加数据库管理员角色）
(12, 2), (12, 5), (12, 9),
-- 用户13 devops_chen（增加运维工程师角色）
(13, 2), (13, 5), (13, 11),
-- 用户14 ai_researcher
(14, 2), (14, 5),
-- 用户15 security_liu（增加安全工程师角色）
(15, 2), (15, 5), (15, 10),
-- 用户16 fullstack_sun
(16, 2), (16, 5),
-- 用户17 test_user1
(17, 2),
-- 用户18 test_user2
(18, 2),
-- 用户19 student_zhang
(19, 2),
-- 用户20 vip_user
(20, 2), (20, 3),
-- 用户21 content_creator
(21, 2), (21, 4), (21, 6),
-- 用户22 code_farmer
(22, 2), (22, 5),
-- 用户23 bug_maker（设为黑名单用户）
(23, 8),
-- 用户24 tech_girl
(24, 2), (24, 5),
-- 用户25 old_driver
(25, 2),
-- 用户26 newbie_coder
(26, 2), (26, 5),
-- 用户27 java_master
(27, 2), (27, 5),
-- 用户28 python_fan
(28, 2), (28, 5),
-- 用户29 golang_dev
(29, 2), (29, 5),
-- 用户30 rustacean
(30, 2), (30, 5),
-- 用户31 frontend_queen
(31, 2), (31, 5),
-- 用户32 backend_king
(32, 2), (32, 5),
-- 用户33 devops_master
(33, 2), (33, 5),
-- 用户34 data_scientist
(34, 2), (34, 5),
-- 用户35 cyber_security
(35, 2), (35, 5),
-- 用户36 blockchain_dev
(36, 2), (36, 5),
-- 用户37 game_developer
(37, 2), (37, 5),
-- 用户38 mobile_expert
(38, 2), (38, 5),
-- 用户39 cloud_architect
(39, 2), (39, 5),
-- 用户40 ai_engineer
(40, 2), (40, 5),
-- 用户41 open_source
(41, 2), (41, 5),
-- 用户42 user_41（额外赋予VIP角色）
(42, 2), (42, 3),
-- 用户43 user_42
(43, 2),
-- 用户44 user_43
(44, 2),
-- 用户45 user_44（额外赋予VIP角色）
(45, 2), (45, 3),
-- 用户46 user_45
(46, 2),
-- 用户47 user_46
(47, 2),
-- 用户48 user_47
(48, 2),
-- 用户49 user_48（额外赋予VIP角色）
(49, 2), (49, 3),
-- 用户50 user_49
(50, 2),
-- 用户51 user_50
(51, 2);

-- 生成user_role表测试数据，user_id从52开始，对应之前创建的user表数据
INSERT INTO `user_role` (`id`, `user_id`, `role_id`, `created_at`, `role_name`) VALUES
(91, 52, 2, '2026-03-16 09:48:57', 'user'),
(92, 53, 2, '2026-03-16 09:49:12', 'user'),
(93, 54, 2, '2026-03-16 09:49:23', 'user'),
(94, 55, 2, '2026-03-16 09:49:35', 'user'),
(95, 56, 2, '2026-03-16 09:49:48', 'user'),
(96, 57, 2, '2026-03-16 09:50:02', 'user'),
(97, 58, 2, '2026-03-16 09:50:15', 'user'),
(98, 59, 2, '2026-03-16 09:50:28', 'user'),
(99, 60, 2, '2026-03-16 09:50:41', 'user'),
(100, 61, 2, '2026-03-16 09:50:54', 'user'),
(101, 62, 2, '2026-03-16 09:51:07', 'user'),
(102, 63, 2, '2026-03-16 09:51:20', 'user'),
(103, 64, 2, '2026-03-16 09:51:33', 'user'),
(104, 65, 2, '2026-03-16 09:51:46', 'user'),
(105, 66, 2, '2026-03-16 09:51:59', 'user'),
(106, 67, 2, '2026-03-16 09:52:12', 'user'),
(107, 68, 2, '2026-03-16 09:52:25', 'user'),
(108, 69, 2, '2026-03-16 09:52:38', 'user'),
(109, 70, 2, '2026-03-16 09:52:51', 'user'),
(110, 71, 2, '2026-03-16 09:53:04', 'user'),
(111, 72, 2, '2026-03-16 09:53:17', 'user'),
(112, 73, 2, '2026-03-16 09:53:30', 'user'),
(113, 74, 2, '2026-03-16 09:53:43', 'user'),
(114, 75, 2, '2026-03-16 09:53:56', 'user'),
(115, 76, 2, '2026-03-16 09:54:09', 'user'),
(116, 77, 2, '2026-03-16 09:54:22', 'user'),
(117, 78, 2, '2026-03-16 09:54:35', 'user'),
(118, 79, 2, '2026-03-16 09:54:48', 'user'),
(119, 80, 2, '2026-03-16 09:55:01', 'user'),
(120, 81, 2, '2026-03-16 09:55:14', 'user'),
(121, 82, 2, '2026-03-16 09:55:27', 'user'),
(122, 83, 2, '2026-03-16 09:55:40', 'user'),
(123, 84, 2, '2026-03-16 09:55:53', 'user'),
(124, 85, 2, '2026-03-16 09:56:06', 'user'),
(125, 86, 2, '2026-03-16 09:56:19', 'user'),
(126, 87, 2, '2026-03-16 09:56:32', 'user'),
(127, 88, 2, '2026-03-16 09:56:45', 'user'),
(128, 89, 2, '2026-03-16 09:56:58', 'user'),
(129, 90, 2, '2026-03-16 09:57:11', 'user'),
(130, 91, 2, '2026-03-16 09:57:24', 'user'),
(131, 92, 2, '2026-03-16 09:57:37', 'user'),
(132, 93, 2, '2026-03-16 09:57:50', 'user'),
(133, 94, 2, '2026-03-16 09:58:03', 'user'),
(134, 95, 2, '2026-03-16 09:58:16', 'user'),
(135, 96, 2, '2026-03-16 09:58:29', 'user'),
(136, 97, 2, '2026-03-16 09:58:42', 'user'),
(137, 98, 2, '2026-03-16 09:58:55', 'user'),
(138, 99, 2, '2026-03-16 09:59:08', 'user'),
(139, 100, 2, '2026-03-16 09:59:21', 'user'),
(140, 101, 2, '2026-03-16 09:59:34', 'user'),
(141, 102, 2, '2026-03-16 09:59:47', 'user'),
(142, 103, 2, '2026-03-16 10:00:00', 'user'),
(143, 104, 2, '2026-03-16 10:00:13', 'user'),
(144, 105, 2, '2026-03-16 10:00:26', 'user'),
(145, 106, 2, '2026-03-16 10:00:39', 'user'),
(146, 107, 2, '2026-03-16 10:00:52', 'user'),
(147, 108, 2, '2026-03-16 10:01:05', 'user'),
(148, 109, 2, '2026-03-16 10:01:18', 'user'),
(149, 110, 2, '2026-03-16 10:01:31', 'user'),
(150, 111, 2, '2026-03-16 10:01:44', 'user'),
(151, 112, 2, '2026-03-16 10:01:57', 'user'),
(152, 113, 2, '2026-03-16 10:02:10', 'user'),
(153, 114, 2, '2026-03-16 10:02:23', 'user'),
(154, 115, 2, '2026-03-16 10:02:36', 'user'),
(155, 116, 2, '2026-03-16 10:02:49', 'user'),
(156, 117, 2, '2026-03-16 10:03:02', 'user'),
(157, 118, 2, '2026-03-16 10:03:15', 'user'),
(158, 119, 2, '2026-03-16 10:03:28', 'user'),
(159, 120, 2, '2026-03-16 10:03:41', 'user'),
(160, 121, 2, '2026-03-16 10:03:54', 'user'),
(161, 122, 2, '2026-03-16 10:04:07', 'user'),
(162, 123, 2, '2026-03-16 10:04:20', 'user'),
(163, 124, 2, '2026-03-16 10:04:33', 'user'),
(164, 125, 2, '2026-03-16 10:04:46', 'user'),
(165, 126, 2, '2026-03-16 10:04:59', 'user'),
(166, 127, 2, '2026-03-16 10:05:12', 'user'),
(167, 128, 2, '2026-03-16 10:05:25', 'user'),
(168, 129, 2, '2026-03-16 10:05:38', 'user'),
(169, 130, 2, '2026-03-16 10:05:51', 'user'),
(170, 131, 2, '2026-03-16 10:06:04', 'user'),
(171, 132, 2, '2026-03-16 10:06:17', 'user'),
(172, 133, 2, '2026-03-16 10:06:30', 'user'),
(173, 134, 2, '2026-03-16 10:06:43', 'user'),
(174, 135, 2, '2026-03-16 10:06:56', 'user'),
(175, 136, 2, '2026-03-16 10:07:09', 'user'),
(176, 137, 2, '2026-03-16 10:07:22', 'user'),
(177, 138, 2, '2026-03-16 10:07:35', 'user'),
(178, 139, 2, '2026-03-16 10:07:48', 'user'),
(179, 140, 2, '2026-03-16 10:08:01', 'user'),
(180, 141, 2, '2026-03-16 10:08:14', 'user'),
(181, 142, 2, '2026-03-16 10:08:27', 'user'),
(182, 143, 2, '2026-03-16 10:08:40', 'user'),
(183, 144, 2, '2026-03-16 10:08:53', 'user'),
(184, 145, 2, '2026-03-16 10:09:06', 'user'),
(185, 146, 2, '2026-03-16 10:09:19', 'user'),
(186, 147, 2, '2026-03-16 10:09:32', 'user'),
(187, 148, 2, '2026-03-16 10:09:45', 'user'),
(188, 149, 2, '2026-03-16 10:09:58', 'user'),
(189, 150, 2, '2026-03-16 10:10:11', 'user'),
(190, 151, 2, '2026-03-16 10:10:24', 'user'),
(191, 152, 2, '2026-03-16 10:10:37', 'user'),
(192, 153, 2, '2026-03-16 10:10:50', 'user'),
(193, 154, 2, '2026-03-16 10:11:03', 'user'),
(194, 155, 2, '2026-03-16 10:11:16', 'user'),
(195, 156, 2, '2026-03-16 10:11:29', 'user'),
(196, 157, 2, '2026-03-16 10:11:42', 'user'),
(197, 158, 2, '2026-03-16 10:11:55', 'user'),
(198, 159, 2, '2026-03-16 10:12:08', 'user'),
(199, 160, 2, '2026-03-16 10:12:21', 'user'),
(200, 161, 2, '2026-03-16 10:12:34', 'user'),
(201, 162, 2, '2026-03-16 10:12:47', 'user'),
(202, 163, 2, '2026-03-16 10:13:00', 'user'),
(203, 164, 2, '2026-03-16 10:13:13', 'user'),
(204, 165, 2, '2026-03-16 10:13:26', 'user'),
(205, 166, 2, '2026-03-16 10:13:39', 'user'),
(206, 167, 2, '2026-03-16 10:13:52', 'user'),
(207, 168, 2, '2026-03-16 10:14:05', 'user'),
(208, 169, 2, '2026-03-16 10:14:18', 'user'),
(209, 170, 2, '2026-03-16 10:14:31', 'user'),
(210, 171, 2, '2026-03-16 10:14:44', 'user'),
(211, 172, 2, '2026-03-16 10:14:57', 'user'),
(212, 173, 2, '2026-03-16 10:15:10', 'user'),
(213, 174, 2, '2026-03-16 10:15:23', 'user'),
(214, 175, 2, '2026-03-16 10:15:36', 'user'),
(215, 176, 2, '2026-03-16 10:15:49', 'user'),
(216, 177, 2, '2026-03-16 10:16:02', 'user'),
(217, 178, 2, '2026-03-16 10:16:15', 'user'),
(218, 179, 2, '2026-03-16 10:16:28', 'user'),
(219, 180, 2, '2026-03-16 10:16:41', 'user'),
(220, 181, 2, '2026-03-16 10:16:54', 'user'),
(221, 182, 2, '2026-03-16 10:17:07', 'user'),
(222, 183, 2, '2026-03-16 10:17:20', 'user'),
(223, 184, 2, '2026-03-16 10:17:33', 'user'),
(224, 185, 2, '2026-03-16 10:17:46', 'user'),
(225, 186, 2, '2026-03-16 10:17:59', 'user'),
(226, 187, 2, '2026-03-16 10:18:12', 'user'),
(227, 188, 2, '2026-03-16 10:18:25', 'user'),
(228, 189, 2, '2026-03-16 10:18:38', 'user'),
(229, 190, 2, '2026-03-16 10:18:51', 'user'),
(230, 191, 2, '2026-03-16 10:19:04', 'user'),
(231, 192, 2, '2026-03-16 10:19:17', 'user'),
(232, 193, 2, '2026-03-16 10:19:30', 'user'),
(233, 194, 2, '2026-03-16 10:19:43', 'user'),
(234, 195, 2, '2026-03-16 10:19:56', 'user'),
(235, 196, 2, '2026-03-16 10:20:09', 'user'),
(236, 197, 2, '2026-03-16 10:20:22', 'user'),
(237, 198, 2, '2026-03-16 10:20:35', 'user'),
(238, 199, 2, '2026-03-16 10:20:48', 'user'),
(239, 200, 2, '2026-03-16 10:21:01', 'user'),
(240, 201, 2, '2026-03-16 10:21:14', 'user'),
(241, 202, 2, '2026-03-16 10:21:27', 'user'),
(242, 203, 2, '2026-03-16 10:21:40', 'user'),
(243, 204, 2, '2026-03-16 10:21:53', 'user'),
(244, 205, 2, '2026-03-16 10:22:06', 'user'),
(245, 206, 2, '2026-03-16 10:22:19', 'user'),
(246, 207, 2, '2026-03-16 10:22:32', 'user'),
(247, 208, 2, '2026-03-16 10:22:45', 'user'),
(248, 209, 2, '2026-03-16 10:22:58', 'user'),
(249, 210, 2, '2026-03-16 10:23:11', 'user'),
(250, 211, 2, '2026-03-16 10:23:24', 'user'),
(251, 212, 2, '2026-03-16 10:23:37', 'user'),
(252, 213, 2, '2026-03-16 10:23:50', 'user'),
(253, 214, 2, '2026-03-16 10:24:03', 'user'),
(254, 215, 2, '2026-03-16 10:24:16', 'user'),
(255, 216, 2, '2026-03-16 10:24:29', 'user'),
(256, 217, 2, '2026-03-16 10:24:42', 'user'),
(257, 218, 2, '2026-03-16 10:24:55', 'user'),
(258, 219, 2, '2026-03-16 10:25:08', 'user'),
(259, 220, 2, '2026-03-16 10:25:21', 'user'),
(260, 221, 2, '2026-03-16 10:25:34', 'user'),
(261, 222, 2, '2026-03-16 10:25:47', 'user'),
(262, 223, 2, '2026-03-16 10:26:00', 'user'),
(263, 224, 2, '2026-03-16 10:26:13', 'user'),
(264, 225, 2, '2026-03-16 10:26:26', 'user'),
(265, 226, 2, '2026-03-16 10:26:39', 'user'),
(266, 227, 2, '2026-03-16 10:26:52', 'user'),
(267, 228, 2, '2026-03-16 10:27:05', 'user'),
(268, 229, 2, '2026-03-16 10:27:18', 'user'),
(269, 230, 2, '2026-03-16 10:27:31', 'user'),
(270, 231, 2, '2026-03-16 10:27:44', 'user'),
(271, 232, 2, '2026-03-16 10:27:57', 'user'),
(272, 233, 2, '2026-03-16 10:28:10', 'user'),
(273, 234, 2, '2026-03-16 10:28:23', 'user'),
(274, 235, 2, '2026-03-16 10:28:36', 'user'),
(275, 236, 2, '2026-03-16 10:28:49', 'user'),
(276, 237, 2, '2026-03-16 10:29:02', 'user'),
(277, 238, 2, '2026-03-16 10:29:15', 'user'),
(278, 239, 2, '2026-03-16 10:29:28', 'user'),
(279, 240, 2, '2026-03-16 10:29:41', 'user'),
(280, 241, 2, '2026-03-16 10:29:54', 'user'),
(281, 242, 2, '2026-03-16 10:30:07', 'user'),
(282, 243, 2, '2026-03-16 10:30:20', 'user'),
(283, 244, 2, '2026-03-16 10:30:33', 'user'),
(284, 245, 2, '2026-03-16 10:30:46', 'user'),
(285, 246, 2, '2026-03-16 10:30:59', 'user'),
(286, 247, 2, '2026-03-16 10:31:12', 'user'),
(287, 248, 2, '2026-03-16 10:31:25', 'user'),
(288, 249, 2, '2026-03-16 10:31:38', 'user'),
(289, 250, 2, '2026-03-16 10:31:51', 'user'),
(290, 251, 2, '2026-03-16 10:32:04', 'user'),
(291, 252, 2, '2026-03-16 10:32:17', 'user'),
(292, 253, 2, '2026-03-16 10:32:30', 'user'),
(293, 254, 2, '2026-03-16 10:32:43', 'user'),
(294, 255, 2, '2026-03-16 10:32:56', 'user'),
(295, 256, 2, '2026-03-16 10:33:09', 'user'),
(296, 257, 2, '2026-03-16 10:33:22', 'user'),
(297, 258, 2, '2026-03-16 10:33:35', 'user'),
(298, 259, 2, '2026-03-16 10:33:48', 'user'),
(299, 260, 2, '2026-03-16 10:34:01', 'user'),
(300, 261, 2, '2026-03-16 10:34:14', 'user'),
(301, 262, 2, '2026-03-16 10:34:27', 'user'),
(302, 263, 2, '2026-03-16 10:34:40', 'user'),
(303, 264, 2, '2026-03-16 10:34:53', 'user'),
(304, 265, 2, '2026-03-16 10:35:06', 'user'),
(305, 266, 2, '2026-03-16 10:35:19', 'user'),
(306, 267, 2, '2026-03-16 10:35:32', 'user'),
(307, 268, 2, '2026-03-16 10:35:45', 'user'),
(308, 269, 2, '2026-03-16 10:35:58', 'user'),
(309, 270, 2, '2026-03-16 10:36:11', 'user'),
(310, 271, 2, '2026-03-16 10:36:24', 'user'),
(311, 272, 2, '2026-03-16 10:36:37', 'user'),
(312, 273, 2, '2026-03-16 10:36:50', 'user'),
(313, 274, 2, '2026-03-16 10:37:03', 'user'),
(314, 275, 2, '2026-03-16 10:37:16', 'user'),
(315, 276, 2, '2026-03-16 10:37:29', 'user'),
(316, 277, 2, '2026-03-16 10:37:42', 'user'),
(317, 278, 2, '2026-03-16 10:37:55', 'user'),
(318, 279, 2, '2026-03-16 10:38:08', 'user'),
(319, 280, 2, '2026-03-16 10:38:21', 'user'),
(320, 281, 2, '2026-03-16 10:38:34', 'user'),
(321, 282, 2, '2026-03-16 10:38:47', 'user'),
(322, 283, 2, '2026-03-16 10:39:00', 'user'),
(323, 284, 2, '2026-03-16 10:39:13', 'user'),
(324, 285, 2, '2026-03-16 10:39:26', 'user'),
(325, 286, 2, '2026-03-16 10:39:39', 'user'),
(326, 287, 2, '2026-03-16 10:39:52', 'user'),
(327, 288, 2, '2026-03-16 10:40:05', 'user'),
(328, 289, 2, '2026-03-16 10:40:18', 'user'),
(329, 290, 2, '2026-03-16 10:40:31', 'user'),
(330, 291, 2, '2026-03-16 10:40:44', 'user'),
(331, 292, 2, '2026-03-16 10:40:57', 'user'),
(332, 293, 2, '2026-03-16 10:41:10', 'user'),
(333, 294, 2, '2026-03-16 10:41:23', 'user'),
(334, 295, 2, '2026-03-16 10:41:36', 'user'),
(335, 296, 2, '2026-03-16 10:41:49', 'user'),
(336, 297, 2, '2026-03-16 10:42:02', 'user'),
(337, 298, 2, '2026-03-16 10:42:15', 'user'),
(338, 299, 2, '2026-03-16 10:42:28', 'user'),
(339, 300, 2, '2026-03-16 10:42:41', 'user'),
(340, 301, 2, '2026-03-16 10:42:54', 'user'),
(341, 302, 2, '2026-03-16 10:43:07', 'user'),
(342, 303, 2, '2026-03-16 10:43:20', 'user'),
(343, 304, 2, '2026-03-16 10:43:33', 'user'),
(344, 305, 2, '2026-03-16 10:43:46', 'user'),
(345, 306, 2, '2026-03-16 10:43:59', 'user'),
(346, 307, 2, '2026-03-16 10:44:12', 'user'),
(347, 308, 2, '2026-03-16 10:44:25', 'user'),
(348, 309, 2, '2026-03-16 10:44:38', 'user'),
(349, 310, 2, '2026-03-16 10:44:51', 'user'),
(350, 311, 2, '2026-03-16 10:45:04', 'user');

INSERT INTO role (id, role_name, role_code, description, status, sort) VALUES
(1, '超级管理员', 'admin', '系统最高权限', '1', 1),
(2, '普通用户', 'user', '普通注册用户', '1', 2),
(3, 'VIP用户', 'vip', 'VIP会员用户', '1', 3),
(4, '版主', 'moderator', '内容管理与社区维护', '1', 4),
(5, '审核员', 'audit', '内容审核人员', '1', 5),
(6, '运营', 'operator', '数据分析与活动运营', '1', 6),
(7, '开发者', 'developer', '开发调试权限', '1', 7);

INSERT INTO permission (id, parent_id, name, permission_code, type, path, component, icon, sort) VALUES
(1,0,'前台','frontend','MENU','/','Layout','home',1),
(2,0,'后台','admin','MENU','/admin','Layout','setting',2);
-- 前台
INSERT INTO permission (id, parent_id, name, permission_code, type, sort) VALUES
(100,1,'文章浏览','article:view','API',1),
(101,1,'文章点赞','article:like','API',2),
(102,1,'文章评论','article:comment','API',3),
(103,1,'发布文章','article:publish','API',4),
(104,1,'收藏文章','article:favorite','API',5),
(105,1,'用户关注','user:follow','API',6);


INSERT INTO permission (id, parent_id, name, permission_code, type, sort) VALUES
(200,2,'文章管理','content:article','MENU',1),
(201,200,'查看文章','content:article:view','API',1),
(202,200,'编辑文章','content:article:edit','API',2),
(203,200,'删除文章','content:article:delete','API',3);

INSERT INTO permission VALUES
(300,2,'评论管理','content:comment','MENU',NULL,NULL,NULL,NULL,NULL,1,'1',NOW(),NOW()),
(301,300,'查看评论','content:comment:view','API',NULL,NULL,NULL,NULL,NULL,1,'1',NOW(),NOW()),
(302,300,'删除评论','content:comment:delete','API',NULL,NULL,NULL,NULL,NULL,2,'1',NOW(),NOW());

INSERT INTO permission VALUES
(400,2,'审核管理','audit','MENU',NULL,NULL,NULL,NULL,NULL,1,'1',NOW(),NOW()),
(401,400,'查看审核','audit:article:view','API',NULL,NULL,NULL,NULL,NULL,1,'1',NOW(),NOW()),
(402,400,'审核通过','audit:article:approve','API',NULL,NULL,NULL,NULL,NULL,2,'1',NOW(),NOW()),
(403,400,'审核拒绝','audit:article:reject','API',NULL,NULL,NULL,NULL,NULL,3,'1',NOW(),NOW());

INSERT INTO permission VALUES
(500,2,'举报管理','report','MENU',NULL,NULL,NULL,NULL,NULL,1,'1',NOW(),NOW()),
(501,500,'查看举报','report:view','API',NULL,NULL,NULL,NULL,NULL,1,'1',NOW(),NOW()),
(502,500,'处理举报','report:process','API',NULL,NULL,NULL,NULL,NULL,2,'1',NOW(),NOW());

INSERT INTO permission VALUES
(600,2,'数据分析','analytics','MENU',NULL,NULL,NULL,NULL,NULL,1,'1',NOW(),NOW()),
(601,600,'用户分析','analytics:user','API',NULL,NULL,NULL,NULL,NULL,1,'1',NOW(),NOW()),
(602,600,'内容分析','analytics:content','API',NULL,NULL,NULL,NULL,NULL,2,'1',NOW(),NOW());

INSERT INTO permission VALUES
(700,2,'系统管理','system','MENU',NULL,NULL,NULL,NULL,NULL,1,'1',NOW(),NOW()),
(701,700,'系统配置','system:config','API',NULL,NULL,NULL,NULL,NULL,1,'1',NOW(),NOW()),
(702,700,'操作日志','system:log','API',NULL,NULL,NULL,NULL,NULL,2,'1',NOW(),NOW());

INSERT INTO permission (id, parent_id, name, permission_code, type, sort) VALUES
(9999,0,'全部权限','all:permission','API',0);

INSERT INTO role_permission (role_id, permission_id) VALUES
(1,9999);
INSERT INTO role_permission (role_id, permission_id) VALUES
(2,100),(2,101),(2,102),(2,103),(2,104),(2,105);
INSERT INTO role_permission (role_id, permission_id) VALUES
(3,100),(3,101),(3,102),(3,103),(3,104),(3,105);
INSERT INTO role_permission (role_id, permission_id) VALUES
(4,201),(4,202),(4,203),
(4,301),(4,302),
(4,501),(4,502);

INSERT INTO role_permission (role_id, permission_id) VALUES
(5,401),(5,402),(5,403);
INSERT INTO role_permission (role_id, permission_id) VALUES
(6,601),(6,602);
INSERT INTO role_permission (role_id, permission_id) VALUES
(7,701),(7,702);



INSERT INTO article (user_id, title, summary, content, content_html, category_id, type, format, status, visibility, is_top, is_recommend, is_original, is_commentable, view_count, like_count, comment_count, collect_count, share_count, word_count, reading_time, published_at, last_comment_time) VALUES

-- 技术分享类文章（15条）
(1001, '三年后端开发，我总结的MySQL调优实战经验', '从索引优化到查询语句，分享我在实际项目中遇到的性能问题及解决方案', '在最近的项目中，我们遇到了数据库查询缓慢的问题...', '<p>在最近的项目中，我们遇到了数据库查询缓慢的问题...</p>', 1, '1', '1', '1', '0', '1', '1', '1', '1', 12560, 890, 156, 320, 210, 3500, 15, '2024-03-10 09:30:00', '2024-03-15 16:20:00'),
(1002, 'Vue3项目从0到上线部署全流程记录', '记录一个企业级Vue3项目从创建到部署的完整过程，包含各种踩坑点', '上周刚完成了一个后台管理系统的开发，这里记录下整个过程...', '<p>上周刚完成了一个后台管理系统的开发，这里记录下整个过程...</p>', 2, '1', '1', '1', '0', '0', '1', '1', '1', 8920, 450, 89, 156, 95, 2800, 12, '2024-03-12 14:20:00', '2024-03-14 11:30:00'),
(1003, '一次线上内存泄漏排查全过程', 'JVM内存溢出？看看我是如何定位和解决这个棘手问题的', '凌晨3点收到报警，服务器内存使用率超过95%...', '<p>凌晨3点收到报警，服务器内存使用率超过95%...</p>', 3, '1', '2', '1', '0', '0', '0', '1', '1', 7540, 320, 67, 120, 78, 2200, 10, '2024-03-05 16:45:00', '2024-03-10 09:15:00'),
(1004, 'Spring Boot整合Redis实现分布式锁', '实际业务中的分布式锁应用，避免重复提交和资源竞争', '在电商秒杀场景中，分布式锁是必不可少的...', '<p>在电商秒杀场景中，分布式锁是必不可少的...</p>', 1, '1', '1', '1', '0', '0', '1', '1', '1', 11020, 680, 132, 245, 156, 3100, 14, '2024-03-08 11:10:00', '2024-03-13 15:40:00'),
(1005, '前端性能优化：从8秒到1.5秒的蜕变', '公司官网首屏加载优化实战，分享具体的技术方案', '客户投诉网站打开太慢，老板下了死命令一周内必须优化...', '<p>客户投诉网站打开太慢，老板下了死命令一周内必须优化...</p>', 2, '1', '2', '1', '0', '1', '1', '1', '1', 9680, 520, 98, 187, 112, 2600, 11, '2024-03-01 10:00:00', '2024-03-07 14:25:00'),
(1006, '微服务架构下的日志收集方案', 'ELK+Filebeat实现分布式系统的日志统一管理', '随着微服务数量的增加，日志分散在各个服务器...', '<p>随着微服务数量的增加，日志分散在各个服务器...</p>', 3, '1', '1', '1', '0', '0', '0', '1', '1', 6320, 290, 45, 98, 64, 1900, 8, '2024-02-28 15:30:00', '2024-03-05 10:50:00'),
(1007, 'Git团队协作规范与最佳实践', '我们团队是如何使用Git进行高效协作的', '刚入职时看到各种混乱的提交记录，现在终于规范起来了...', '<p>刚入职时看到各种混乱的提交记录，现在终于规范起来了...</p>', 4, '1', '1', '1', '0', '0', '1', '1', '1', 14200, 780, 168, 356, 198, 4200, 18, '2024-03-03 13:45:00', '2024-03-12 17:30:00'),
(1008, 'Docker从入门到生产环境部署', '手把手教你用Docker部署一个完整的Web应用', '以前部署应用总是一堆环境问题，现在用Docker简单多了...', '<p>以前部署应用总是一堆环境问题，现在用Docker简单多了...</p>', 3, '1', '2', '1', '0', '0', '1', '1', '1', 11560, 620, 124, 278, 145, 3800, 16, '2024-03-06 09:15:00', '2024-03-11 14:20:00'),
(1009, 'React Hooks使用中的常见陷阱', '总结我在使用Hooks过程中遇到的那些坑', 'useEffect的依赖数组、useCallback的使用时机...', '<p>useEffect的依赖数组、useCallback的使用时机...</p>', 2, '1', '1', '1', '0', '0', '0', '1', '1', 8760, 410, 76, 143, 87, 2400, 10, '2024-03-04 16:50:00', '2024-03-09 11:45:00'),
(1010, '从零搭建一个企业级Node.js后端服务', '包含用户认证、权限管理、日志记录等完整功能', '最近接了个外包项目，需要快速搭建一个后端服务...', '<p>最近接了个外包项目，需要快速搭建一个后端服务...</p>', 1, '1', '2', '1', '0', '1', '1', '1', '1', 9980, 550, 112, 210, 124, 3500, 15, '2024-03-02 14:10:00', '2024-03-08 16:30:00'),
(1011, 'SQL优化：如何让慢查询快起来', '实际案例分析，那些让查询速度提升10倍的方法', '昨天优化了一个执行时间超过30秒的查询，现在只要2秒...', '<p>昨天优化了一个执行时间超过30秒的查询，现在只要2秒...</p>', 1, '1', '1', '1', '0', '0', '1', '1', '1', 7240, 380, 68, 125, 76, 2100, 9, '2024-02-25 11:20:00', '2024-03-03 13:15:00'),
(1012, '微信小程序开发避坑指南', '开发了5个小程序后，我总结的这些经验', '审核被拒、性能问题、兼容性处理...都遇到过', '<p>审核被拒、性能问题、兼容性处理...都遇到过</p>', 2, '1', '2', '1', '0', '0', '0', '1', '1', 15800, 920, 198, 412, 256, 4800, 20, '2024-03-07 10:40:00', '2024-03-14 18:45:00'),
(1013, 'Linux服务器安全加固方案', '防止服务器被黑的15条安全措施', '同事的服务器被挖矿了，吓得我赶紧检查自己的服务器...', '<p>同事的服务器被挖矿了，吓得我赶紧检查自己的服务器...</p>', 3, '1', '1', '1', '0', '0', '1', '1', '1', 8430, 470, 82, 156, 92, 2300, 10, '2024-02-29 15:25:00', '2024-03-06 12:40:00'),
(1014, 'TypeScript在大型项目中的应用实践', '从JavaScript迁移到TypeScript的经验分享', '公司项目有10万行代码，迁移过程虽然痛苦但值得...', '<p>公司项目有10万行代码，迁移过程虽然痛苦但值得...</p>', 2, '1', '1', '1', '0', '0', '0', '1', '1', 6920, 350, 58, 110, 68, 2000, 8, '2024-02-27 09:50:00', '2024-03-04 14:15:00'),
(1015, 'Kafka在订单系统中的应用', '如何用Kafka解耦订单处理流程', '双11期间订单量暴涨，幸好用了Kafka做异步处理...', '<p>双11期间订单量暴涨，幸好用了Kafka做异步处理...</p>', 3, '1', '2', '1', '0', '1', '1', '1', '1', 10780, 590, 128, 245, 134, 3300, 14, '2024-03-09 13:35:00', '2024-03-15 10:25:00'),

-- 问答类文章（10条）
(1016, '应届生选择Java还是Go？求前辈指点', '校招拿到了两个offer，一个用Java一个用Go，该如何选择？', '本人双非本科，今年6月毕业，现在有两个offer...', '<p>本人双非本科，今年6月毕业，现在有两个offer...</p>', 4, '2', '1', '1', '0', '0', '0', '1', '1', 4520, 120, 156, 45, 32, 800, 4, '2024-03-11 16:20:00', '2024-03-15 14:35:00'),
(1017, '35岁程序员出路在哪里？', '马上35了，感觉技术提升遇到瓶颈，大家有什么建议？', '在一线城市做后端开发8年了，现在有点迷茫...', '<p>在一线城市做后端开发8年了，现在有点迷茫...</p>', 5, '2', '1', '1', '0', '1', '1', '1', '1', 12800, 680, 432, 156, 198, 1200, 6, '2024-03-05 10:15:00', '2024-03-14 20:30:00'),
(1018, '外包公司值得去吗？', '收到一个外包到腾讯的offer，薪资比现在高30%', '现在在一家小公司，有点想跳槽但又担心外包经历...', '<p>现在在一家小公司，有点想跳槽但又担心外包经历...</p>', 5, '2', '1', '1', '0', '0', '0', '1', '1', 6720, 210, 287, 78, 56, 900, 5, '2024-03-08 14:40:00', '2024-03-13 18:15:00'),
(1019, '如何准备大厂算法面试？', '刷了200道LeetCode还是没信心，求大佬指点', '目标进字节跳动，目前刷题进度200/500...', '<p>目标进字节跳动，目前刷题进度200/500...</p>', 4, '2', '1', '1', '0', '0', '1', '1', '1', 8920, 340, 189, 112, 76, 1100, 5, '2024-03-02 11:30:00', '2024-03-10 16:45:00'),
(1020, '转行学编程来得及吗？', '28岁土木工程想转行IT，还有机会吗？', '本科土木，工地干了3年，实在受不了想转行...', '<p>本科土木，工地干了3年，实在受不了想转行...</p>', 5, '2', '1', '1', '0', '0', '0', '1', '1', 10560, 420, 345, 98, 89, 1500, 7, '2024-02-26 09:45:00', '2024-03-07 15:20:00'),
(1021, '考研还是直接工作？', '普通一本计算机专业，纠结要不要考研', '家里条件一般，想早点工作，但又担心学历不够...', '<p>家里条件一般，想早点工作，但又担心学历不够...</p>', 5, '2', '1', '1', '0', '0', '0', '1', '1', 7640, 290, 198, 67, 45, 1000, 5, '2024-03-04 15:10:00', '2024-03-12 11:25:00'),
(1022, '前端现在是不是饱和了？', '看招聘要求越来越高，担心找不到工作', '自学前端半年，感觉技术更新太快跟不上...', '<p>自学前端半年，感觉技术更新太快跟不上...</p>', 4, '2', '1', '1', '0', '0', '0', '1', '1', 5340, 180, 156, 45, 32, 700, 4, '2024-03-10 13:25:00', '2024-03-15 09:40:00'),
(1023, '公司技术栈太老要不要离职？', '还在用jQuery和Spring MVC，学不到新技术', '毕业两年，公司项目都是老技术，怕以后没竞争力...', '<p>毕业两年，公司项目都是老技术，怕以后没竞争力...</p>', 5, '2', '1', '1', '0', '0', '1', '1', '1', 6820, 250, 187, 56, 42, 950, 5, '2024-03-01 16:50:00', '2024-03-09 14:15:00'),
(1024, '该不该接私活？', '朋友介绍了个小项目，大概2万块，但怕影响工作', '本职工作不忙，想赚点外快但又担心有风险...', '<p>本职工作不忙，想赚点外快但又担心有风险...</p>', 5, '2', '1', '1', '0', '0', '0', '1', '1', 4890, 160, 134, 38, 28, 650, 3, '2024-03-06 10:35:00', '2024-03-11 17:50:00'),
(1025, '如何快速熟悉一个大型代码库？', '刚入职新公司，代码几十万行看不懂怎么办', '第一周完全懵，mentor也没时间详细讲...', '<p>第一周完全懵，mentor也没时间详细讲...</p>', 4, '2', '1', '1', '0', '0', '0', '1', '1', 6120, 230, 145, 52, 39, 850, 4, '2024-03-07 14:20:00', '2024-03-13 10:30:00'),

-- 分享类文章（10条）
(1026, '分享一下我的工作台配置', '程序员的高效工作环境是怎样的？', '双显示器、机械键盘、各种开发工具配置...', '<p>双显示器、机械键盘、各种开发工具配置...</p>', 6, '3', '2', '1', '0', '0', '1', '1', '1', 7230, 410, 89, 156, 78, 1800, 8, '2024-03-03 15:40:00', '2024-03-10 14:25:00'),
(1027, '推荐几个提升效率的Chrome插件', '这些插件让我每天节省至少1小时', '广告拦截、笔记工具、代码格式化插件等等...', '<p>广告拦截、笔记工具、代码格式化插件等等...</p>', 6, '3', '1', '1', '0', '0', '0', '1', '1', 15600, 780, 167, 345, 198, 2200, 9, '2024-03-08 09:15:00', '2024-03-14 19:30:00'),
(1028, '我的书单：2023年读过的好书', '技术、产品、思维类书籍推荐', '今年读了30多本书，筛选出最值得推荐的10本...', '<p>今年读了30多本书，筛选出最值得推荐的10本...</p>', 6, '3', '2', '1', '0', '0', '0', '1', '1', 8920, 460, 98, 187, 112, 2500, 10, '2024-02-28 13:25:00', '2024-03-06 16:40:00'),
(1029, '免费编程学习资源合集', 'B站、YouTube、GitHub上的优质资源', '自学编程这么多年，整理了一些免费的好资源...', '<p>自学编程这么多年，整理了一些免费的好资源...</p>', 4, '3', '1', '1', '0', '1', '1', '1', '1', 23400, 1200, 256, 512, 345, 3200, 13, '2024-03-05 11:30:00', '2024-03-15 08:45:00'),
(1030, '程序员健康指南：拯救你的颈椎', '长期码字如何保持健康？', '最近颈椎痛得厉害，开始研究如何改善...', '<p>最近颈椎痛得厉害，开始研究如何改善...</p>', 6, '3', '2', '1', '0', '0', '0', '1', '1', 10320, 520, 123, 234, 145, 1900, 8, '2024-03-01 14:50:00', '2024-03-08 12:15:00'),
(1031, '我的副业收入来源分享', '程序员搞副业的几种可行方式', '除了工资，我通过这几种方式每月多赚1万+...', '<p>除了工资，我通过这几种方式每月多赚1万+...</p>', 5, '3', '1', '1', '2', '0', '1', '1', '1', 18700, 890, 287, 456, 298, 2800, 12, '2024-03-06 16:20:00', '2024-03-13 21:10:00'),
(1032, '开源项目维护经验谈', '维护一个500+ star项目的感受', '去年开源了一个工具库，没想到火了，分享一些经验...', '<p>去年开源了一个工具库，没想到火了，分享一些经验...</p>', 4, '3', '1', '1', '0', '0', '1', '1', '1', 7640, 380, 76, 156, 87, 2100, 9, '2024-02-27 10:45:00', '2024-03-05 15:30:00'),
(1033, '技术博客写作心得', '坚持写博客3年，我的收获与建议', '从零开始写技术博客，现在月访问量过万...', '<p>从零开始写技术博客，现在月访问量过万...</p>', 6, '3', '2', '1', '0', '0', '0', '1', '1', 9820, 490, 112, 210, 124, 2400, 10, '2024-03-04 15:35:00', '2024-03-11 17:20:00'),
(1034, '远程工作一年体验', '在家办公是种什么感受？', '疫情后开始远程工作，分享真实体验...', '<p>疫情后开始远程工作，分享真实体验...</p>', 5, '3', '1', '1', '0', '0', '0', '1', '1', 11230, 560, 134, 256, 156, 2600, 11, '2024-03-02 11:10:00', '2024-03-09 19:45:00'),
(1035, '我的年度总结：2023技术成长', '复盘过去一年的学习与项目', '又到年底，总结一下这一年的技术成长...', '<p>又到年底，总结一下这一年的技术成长...</p>', 6, '3', '2', '1', '0', '0', '1', '1', '1', 8430, 420, 89, 178, 98, 2300, 10, '2024-02-26 09:20:00', '2024-03-07 14:50:00'),

-- 更多技术文章（10条）
(1036, 'WebSocket实现实时聊天功能', 'Spring Boot + Vue3实战', '最近做了个在线客服系统，这里分享WebSocket的实现...', '<p>最近做了个在线客服系统，这里分享WebSocket的实现...</p>', 1, '1', '1', '1', '0', '0', '0', '1', '1', 6720, 320, 67, 134, 76, 1800, 8, '2024-03-09 10:25:00', '2024-03-14 15:40:00'),
(1037, '前端错误监控系统搭建', 'Sentry自建与接入指南', '线上bug频发？你需要一个完善的错误监控系统...', '<p>线上bug频发？你需要一个完善的错误监控系统...</p>', 2, '1', '2', '1', '0', '0', '1', '1', '1', 5890, 280, 45, 98, 56, 1500, 7, '2024-02-24 14:15:00', '2024-03-02 11:30:00'),
(1038, '微服务网关选型对比', 'Spring Cloud Gateway vs Zuul', '两个网关都用过，谈谈各自的优缺点...', '<p>两个网关都用过，谈谈各自的优缺点...</p>', 3, '1', '1', '1', '0', '0', '0', '1', '1', 5120, 240, 38, 76, 42, 1200, 5, '2024-03-11 15:30:00', '2024-03-15 13:20:00'),
(1039, 'Python爬虫反反爬策略', '应对各种反爬机制的技巧', '爬虫写多了，总会遇到各种反爬，总结一下应对方法...', '<p>爬虫写多了，总会遇到各种反爬，总结一下应对方法...</p>', 1, '1', '2', '1', '0', '0', '0', '1', '1', 9320, 450, 89, 187, 98, 2700, 12, '2024-03-07 13:45:00', '2024-03-12 16:50:00'),
(1040, '移动端H5适配方案总结', 'vw、rem、flexible方案对比', '做了3年移动端开发，总结一下各种适配方案...', '<p>做了3年移动端开发，总结一下各种适配方案...</p>', 2, '1', '1', '1', '0', '0', '1', '1', '1', 7620, 360, 67, 145, 78, 2200, 10, '2024-02-22 10:10:00', '2024-02-28 14:25:00'),
(1041, '数据库分库分表实践', '千万级数据表的拆分方案', '用户表超过3000万记录，不得不考虑分表了...', '<p>用户表超过3000万记录，不得不考虑分表了...</p>', 1, '1', '2', '1', '0', '1', '1', '1', '1', 11560, 580, 123, 256, 145, 3500, 15, '2024-03-04 16:20:00', '2024-03-11 19:30:00'),
(1042, 'Jenkins Pipeline自动化部署', '编写高效的CI/CD流水线', '手动部署太麻烦？试试Pipeline自动化...', '<p>手动部署太麻烦？试试Pipeline自动化...</p>', 3, '1', '1', '1', '0', '0', '0', '1', '1', 6890, 310, 56, 112, 64, 1900, 8, '2024-02-25 11:40:00', '2024-03-03 15:15:00'),
(1043, 'React性能优化实战', 'memo、useCallback、虚拟列表的应用', '列表渲染卡顿？试试这些优化技巧...', '<p>列表渲染卡顿？试试这些优化技巧...</p>', 2, '1', '1', '1', '0', '0', '1', '1', '1', 8430, 420, 78, 156, 87, 2400, 11, '2024-03-01 09:50:00', '2024-03-08 17:40:00'),
(1044, 'Nginx配置SSL证书', 'HTTPS部署完整教程', '网站必须上HTTPS了，记录一下配置过程...', '<p>网站必须上HTTPS了，记录一下配置过程...</p>', 3, '1', '2', '1', '0', '0', '0', '1', '1', 9230, 460, 89, 187, 102, 2100, 9, '2024-03-10 14:35:00', '2024-03-15 11:25:00'),
(1045, 'Elasticsearch入门教程', '从安装到查询的完整指南', '最近项目要用ES做搜索，学习记录分享一下...', '<p>最近项目要用ES做搜索，学习记录分享一下...</p>', 3, '1', '1', '1', '0', '0', '1', '1', '1', 12560, 620, 134, 278, 156, 3800, 16, '2024-03-06 10:05:00', '2024-03-13 14:50:00');


-- 生成article表测试数据，id从53开始
INSERT INTO `article` (`id`, `user_id`, `title`, `summary`, `content`, `content_html`, `cover_image`, `category_id`, `type`, `format`, `status`, `visibility`, `is_original`, `view_count`, `like_count`, `comment_count`, `collect_count`, `published_at`, `created_at`) VALUES
(53, 52, '人工智能的未来发展趋势', '探讨AI技术在未来5-10年的发展方向和应用场景', '人工智能正在以前所未有的速度发展，从深度学习到大语言模型，技术迭代不断加速。本文将探讨AI技术的未来发展趋势，包括多模态模型、边缘计算、AI伦理等方向。', '<h1>人工智能的未来发展趋势</h1><p>人工智能正在以前所未有的速度发展，从深度学习到大语言模型，技术迭代不断加速。本文将探讨AI技术的未来发展趋势，包括多模态模型、边缘计算、AI伦理等方向。</p>', 'http://127.0.0.1:9000/article/cover/53.jpg', 1, '1', '2', '1', '0', '1', 1250, 89, 23, 45, '2026-02-10 10:30:00', '2026-02-05 08:15:00'),
(54, 53, '深度学习入门指南', '为零基础开发者准备的深度学习学习路径和资源推荐', '深度学习是机器学习的一个重要分支，通过模拟人脑神经网络来处理数据。本文为初学者提供完整的学习路径，包括数学基础、框架选择、实战项目等。', '<h1>深度学习入门指南</h1><p>深度学习是机器学习的一个重要分支，通过模拟人脑神经网络来处理数据。本文为初学者提供完整的学习路径，包括数学基础、框架选择、实战项目等。</p>', 'http://127.0.0.1:9000/article/cover/54.jpg', 1, '1', '1', '1', '0', '1', 2300, 156, 34, 67, '2026-02-12 14:20:00', '2026-02-08 09:30:00'),
(55, 54, 'Python编程技巧20则', '提高Python编程效率的20个实用技巧', 'Python作为最流行的编程语言之一，有很多鲜为人知但非常实用的技巧。本文总结了20个提高编码效率的Python技巧，涵盖列表推导、生成器、装饰器等。', '<h1>Python编程技巧20则</h1><p>Python作为最流行的编程语言之一，有很多鲜为人知但非常实用的技巧。本文总结了20个提高编码效率的Python技巧，涵盖列表推导、生成器、装饰器等。</p>', 'http://127.0.0.1:9000/article/cover/55.jpg', 2, '1', '1', '1', '0', '1', 3450, 234, 56, 89, '2026-02-15 09:45:00', '2026-02-10 11:20:00'),
(56, 55, '微服务架构设计模式', '深入浅出微服务架构的常见设计模式和实践经验', '微服务架构已经成为现代应用开发的主流选择。本文介绍微服务架构中的常见设计模式，包括服务发现、配置管理、API网关、断路器模式等。', '<h1>微服务架构设计模式</h1><p>微服务架构已经成为现代应用开发的主流选择。本文介绍微服务架构中的常见设计模式，包括服务发现、配置管理、API网关、断路器模式等。</p>', 'http://127.0.0.1:9000/article/cover/56.jpg', 3, '1', '2', '1', '0', '1', 1780, 112, 28, 56, '2026-02-18 16:30:00', '2026-02-12 13:45:00'),
(57, 56, '数据库索引优化实战', '如何设计和优化数据库索引以提升查询性能', '数据库索引是提升查询性能的关键。本文从实战角度出发，讲解索引的设计原则、优化技巧，以及如何通过explain分析执行计划。', '<h1>数据库索引优化实战</h1><p>数据库索引是提升查询性能的关键。本文从实战角度出发，讲解索引的设计原则、优化技巧，以及如何通过explain分析执行计划。</p>', 'http://127.0.0.1:9000/article/cover/57.jpg', 4, '1', '1', '1', '0', '1', 980, 67, 15, 34, '2026-02-20 11:15:00', '2026-02-15 10:00:00'),
(58, 57, 'RESTful API设计规范', '构建优雅、易用的RESTful API接口设计指南', 'RESTful API是现代Web服务的基石。本文详细介绍RESTful API的设计原则、最佳实践，包括资源命名、HTTP方法选择、状态码使用等。', '<h1>RESTful API设计规范</h1><p>RESTful API是现代Web服务的基石。本文详细介绍RESTful API的设计原则、最佳实践，包括资源命名、HTTP方法选择、状态码使用等。</p>', 'http://127.0.0.1:9000/article/cover/58.jpg', 2, '1', '2', '1', '0', '1', 1560, 98, 24, 45, '2026-02-22 13:40:00', '2026-02-17 14:30:00'),
(59, 58, 'Git工作流最佳实践', '团队协作中的Git分支管理策略和规范', 'Git是现代软件开发必备的版本控制工具。本文介绍几种主流的Git工作流，包括Git Flow、GitHub Flow、GitLab Flow，并给出实践建议。', '<h1>Git工作流最佳实践</h1><p>Git是现代软件开发必备的版本控制工具。本文介绍几种主流的Git工作流，包括Git Flow、GitHub Flow、GitLab Flow，并给出实践建议。</p>', 'http://127.0.0.1:9000/article/cover/59.jpg', 5, '1', '1', '1', '0', '1', 1120, 76, 18, 32, '2026-02-24 09:20:00', '2026-02-19 09:15:00'),
(60, 59, 'Docker容器化部署实战', '使用Docker实现应用的容器化部署和编排', 'Docker改变了应用的部署方式。本文通过实战案例，讲解如何使用Docker打包应用、管理容器，以及使用Docker Compose进行多容器编排。', '<h1>Docker容器化部署实战</h1><p>Docker改变了应用的部署方式。本文通过实战案例，讲解如何使用Docker打包应用、管理容器，以及使用Docker Compose进行多容器编排。</p>', 'http://127.0.0.1:9000/article/cover/60.jpg', 3, '1', '2', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-02-21 16:45:00'),
(61, 60, '前端工程化实践', '构建高效、可维护的前端项目工程化体系', '前端工程化是大型项目开发的必由之路。本文介绍前端工程化的核心概念和实践，包括模块化、组件化、自动化构建、代码规范等。', '<h1>前端工程化实践</h1><p>前端工程化是大型项目开发的必由之路。本文介绍前端工程化的核心概念和实践，包括模块化、组件化、自动化构建、代码规范等。</p>', 'http://127.0.0.1:9000/article/cover/61.jpg', 2, '1', '1', '1', '0', '1', 890, 54, 12, 23, '2026-02-25 10:30:00', '2026-02-20 11:30:00'),
(62, 61, 'Java并发编程详解', '深入理解Java并发编程的核心概念和实践技巧', 'Java并发编程是后端开发的核心技能。本文详解Java并发编程的基础知识，包括线程创建、同步机制、锁、并发容器等。', '<h1>Java并发编程详解</h1><p>Java并发编程是后端开发的核心技能。本文详解Java并发编程的基础知识，包括线程创建、同步机制、锁、并发容器等。</p>', 'http://127.0.0.1:9000/article/cover/62.jpg', 1, '1', '2', '1', '0', '1', 2340, 145, 32, 67, '2026-02-27 14:15:00', '2026-02-22 13:20:00'),
(63, 62, '网络安全入门指南', '了解Web安全的基础知识和常见攻击防御', '网络安全是每个开发者都应关注的领域。本文介绍Web安全的常见威胁，包括XSS、CSRF、SQL注入等，并给出防御措施。', '<h1>网络安全入门指南</h1><p>网络安全是每个开发者都应关注的领域。本文介绍Web安全的常见威胁，包括XSS、CSRF、SQL注入等，并给出防御措施。</p>', 'http://127.0.0.1:9000/article/cover/63.jpg', 6, '1', '1', '3', '0', '1', 0, 0, 0, 0, NULL, '2026-02-24 10:45:00'),
(64, 63, '程序员健康指南', '长时间编程如何保持身体健康', '程序员是久坐族，健康问题不容忽视。本文分享程序员如何通过正确坐姿、定时运动、眼部保健等方式保持身体健康。', '<h1>程序员健康指南</h1><p>程序员是久坐族，健康问题不容忽视。本文分享程序员如何通过正确坐姿、定时运动、眼部保健等方式保持身体健康。</p>', 'http://127.0.0.1:9000/article/cover/64.jpg', 7, '2', '2', '1', '0', '1', 3450, 210, 45, 78, '2026-03-01 08:30:00', '2026-02-25 15:30:00'),
(65, 64, 'Vue3组合式API实战', '从Options API到Composition API的进阶指南', 'Vue3的组合式API提供了更灵活的代码组织方式。本文通过实战案例，讲解如何使用组合式API重构Vue应用，提高代码复用性。', '<h1>Vue3组合式API实战</h1><p>Vue3的组合式API提供了更灵活的代码组织方式。本文通过实战案例，讲解如何使用组合式API重构Vue应用，提高代码复用性。</p>', 'http://127.0.0.1:9000/article/cover/65.jpg', 2, '1', '1', '1', '0', '1', 1670, 98, 21, 43, '2026-03-02 11:45:00', '2026-02-26 09:20:00'),
(66, 65, 'Redis缓存设计与优化', '高性能Redis缓存系统的设计与实践', 'Redis作为高性能缓存数据库，在系统中扮演重要角色。本文介绍Redis的数据结构、缓存策略、持久化机制以及常见问题解决方案。', '<h1>Redis缓存设计与优化</h1><p>Redis作为高性能缓存数据库，在系统中扮演重要角色。本文介绍Redis的数据结构、缓存策略、持久化机制以及常见问题解决方案。</p>', 'http://127.0.0.1:9000/article/cover/66.jpg', 4, '1', '2', '1', '0', '1', 890, 67, 14, 28, '2026-03-03 15:20:00', '2026-02-27 14:15:00'),
(67, 66, '云原生架构演进', '从传统架构到云原生架构的演进之路', '云原生架构充分利用云计算优势，提升应用的弹性和可移植性。本文介绍云原生的核心概念，包括容器化、微服务、DevOps、声明式API等。', '<h1>云原生架构演进</h1><p>云原生架构充分利用云计算优势，提升应用的弹性和可移植性。本文介绍云原生的核心概念，包括容器化、微服务、DevOps、声明式API等。</p>', 'http://127.0.0.1:9000/article/cover/67.jpg', 3, '1', '1', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-02-28 16:30:00'),
(68, 67, 'MySQL性能调优实战', 'MySQL数据库性能优化的方法论和实践案例', 'MySQL是最流行的关系型数据库之一。本文从硬件、配置、SQL语句、索引等多个维度，分享MySQL性能调优的经验和方法。', '<h1>MySQL性能调优实战</h1><p>MySQL是最流行的关系型数据库之一。本文从硬件、配置、SQL语句、索引等多个维度，分享MySQL性能调优的经验和方法。</p>', 'http://127.0.0.1:9000/article/cover/68.jpg', 4, '1', '2', '1', '0', '1', 2780, 178, 42, 87, '2026-03-04 09:30:00', '2026-03-01 10:45:00'),
(69, 68, 'React Hooks深入浅出', '全面掌握React Hooks的使用技巧和原理', 'React Hooks改变了React组件的编写方式。本文深入讲解常用Hooks的使用方法、实现原理，以及自定义Hooks的最佳实践。', '<h1>React Hooks深入浅出</h1><p>React Hooks改变了React组件的编写方式。本文深入讲解常用Hooks的使用方法、实现原理，以及自定义Hooks的最佳实践。</p>', 'http://127.0.0.1:9000/article/cover/69.jpg', 2, '1', '1', '1', '0', '1', 1980, 134, 28, 56, '2026-03-05 13:15:00', '2026-03-02 11:30:00'),
(70, 69, '程序员面试指南', '技术面试准备攻略和常见问题解析', '技术面试是程序员求职的关键环节。本文分享面试准备方法、常见算法题解析、系统设计面试技巧，以及行为面试经验。', '<h1>程序员面试指南</h1><p>技术面试是程序员求职的关键环节。本文分享面试准备方法、常见算法题解析、系统设计面试技巧，以及行为面试经验。</p>', 'http://127.0.0.1:9000/article/cover/70.jpg', 8, '1', '2', '1', '0', '1', 4560, 320, 67, 123, '2026-03-06 10:00:00', '2026-03-03 14:20:00'),
(71, 70, '设计模式在项目中的应用', '23种设计模式在实际项目中的使用场景和案例', '设计模式是软件工程的经验总结。本文通过实际项目案例，讲解常用设计模式的应用场景、实现方式以及如何避免滥用。', '<h1>设计模式在项目中的应用</h1><p>设计模式是软件工程的经验总结。本文通过实际项目案例，讲解常用设计模式的应用场景、实现方式以及如何避免滥用。</p>', 'http://127.0.0.1:9000/article/cover/71.jpg', 1, '1', '1', '1', '0', '1', 1230, 87, 19, 34, '2026-03-07 14:45:00', '2026-03-04 09:15:00'),
(72, 71, 'TypeScript进阶指南', '掌握TypeScript高级类型和工程化配置', 'TypeScript为JavaScript提供类型系统，提升代码质量和开发体验。本文介绍TypeScript的高级类型、泛型、装饰器以及工程化配置。', '<h1>TypeScript进阶指南</h1><p>TypeScript为JavaScript提供类型系统，提升代码质量和开发体验。本文介绍TypeScript的高级类型、泛型、装饰器以及工程化配置。</p>', 'http://127.0.0.1:9000/article/cover/72.jpg', 2, '1', '2', '1', '0', '1', 890, 56, 12, 23, '2026-03-08 11:20:00', '2026-03-05 13:40:00'),
(73, 72, 'Spring Boot微服务实践', '使用Spring Boot构建微服务应用的完整指南', 'Spring Boot简化了Spring应用的开发。本文从零开始，讲解如何使用Spring Boot构建RESTful API、集成数据库、实现安全认证等。', '<h1>Spring Boot微服务实践</h1><p>Spring Boot简化了Spring应用的开发。本文从零开始，讲解如何使用Spring Boot构建RESTful API、集成数据库、实现安全认证等。</p>', 'http://127.0.0.1:9000/article/cover/73.jpg', 3, '1', '1', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-03-06 15:30:00'),
(74, 73, 'Web性能优化指南', '提升Web应用加载速度和用户体验的优化技巧', 'Web性能直接影响用户体验和业务转化。本文介绍前端性能优化的各个方面，包括资源加载、渲染优化、缓存策略、CDN加速等。', '<h1>Web性能优化指南</h1><p>Web性能直接影响用户体验和业务转化。本文介绍前端性能优化的各个方面，包括资源加载、渲染优化、缓存策略、CDN加速等。</p>', 'http://127.0.0.1:9000/article/cover/74.jpg', 2, '1', '2', '1', '0', '1', 2340, 145, 34, 67, '2026-03-09 09:30:00', '2026-03-07 10:15:00'),
(75, 74, '数据可视化实战', '使用ECharts和D3.js实现数据可视化', '数据可视化让数据更易理解。本文通过实战案例，讲解如何使用ECharts快速构建图表，以及如何使用D3.js实现自定义可视化。', '<h1>数据可视化实战</h1><p>数据可视化让数据更易理解。本文通过实战案例，讲解如何使用ECharts快速构建图表，以及如何使用D3.js实现自定义可视化。</p>', 'http://127.0.0.1:9000/article/cover/75.jpg', 9, '1', '1', '1', '0', '1', 1120, 78, 16, 29, '2026-03-10 15:40:00', '2026-03-08 14:25:00'),
(76, 75, 'Linux运维实战技巧', 'Linux系统管理和运维的实用命令和经验', 'Linux是服务器端的操作系统王者。本文分享Linux系统管理的常用命令、性能监控、日志分析、Shell编程等实用技巧。', '<h1>Linux运维实战技巧</h1><p>Linux是服务器端的操作系统王者。本文分享Linux系统管理的常用命令、性能监控、日志分析、Shell编程等实用技巧。</p>', 'http://127.0.0.1:9000/article/cover/76.jpg', 3, '1', '2', '1', '0', '1', 1670, 98, 21, 45, '2026-03-11 10:15:00', '2026-03-09 09:45:00'),
(77, 76, '消息队列选型指南', 'Kafka、RabbitMQ、RocketMQ对比与实践', '消息队列是分布式系统的核心组件。本文对比主流消息队列的特性、适用场景，并给出选型建议和最佳实践。', '<h1>消息队列选型指南</h1><p>消息队列是分布式系统的核心组件。本文对比主流消息队列的特性、适用场景，并给出选型建议和最佳实践。</p>', 'http://127.0.0.1:9000/article/cover/77.jpg', 3, '1', '1', '1', '0', '1', 1450, 87, 18, 37, '2026-03-12 13:20:00', '2026-03-10 11:30:00'),
(78, 77, '程序员软技能提升', '技术之外的职场软技能培养指南', '技术能力之外，沟通、协作、时间管理等软技能同样重要。本文分享程序员如何提升职场软技能，实现更好的职业发展。', '<h1>程序员软技能提升</h1><p>技术能力之外，沟通、协作、时间管理等软技能同样重要。本文分享程序员如何提升职场软技能，实现更好的职业发展。</p>', 'http://127.0.0.1:9000/article/cover/78.jpg', 8, '2', '2', '1', '0', '1', 2340, 156, 34, 56, '2026-03-13 09:45:00', '2026-03-11 13:20:00'),
(79, 78, '算法竞赛入门', 'ACM/ICPC和LeetCode算法竞赛备战攻略', '算法竞赛是提升编程能力的有效途径。本文介绍算法竞赛的常见题型、解题思路、刷题方法和备赛经验。', '<h1>算法竞赛入门</h1><p>算法竞赛是提升编程能力的有效途径。本文介绍算法竞赛的常见题型、解题思路、刷题方法和备赛经验。</p>', 'http://127.0.0.1:9000/article/cover/79.jpg', 1, '1', '1', '3', '0', '1', 0, 0, 0, 0, NULL, '2026-03-12 15:40:00'),
(80, 79, '个人博客搭建指南', '从零开始搭建技术博客的全流程', '技术博客是程序员知识沉淀和品牌建设的重要方式。本文分享如何选择博客平台、配置域名、优化SEO，以及如何坚持写作。', '<h1>个人博客搭建指南</h1><p>技术博客是程序员知识沉淀和品牌建设的重要方式。本文分享如何选择博客平台、配置域名、优化SEO，以及如何坚持写作。</p>', 'http://127.0.0.1:9000/article/cover/80.jpg', 8, '2', '2', '1', '0', '1', 890, 67, 14, 23, '2026-03-14 11:30:00', '2026-03-12 09:15:00'),
(81, 80, '代码重构之道', '如何安全高效地重构遗留代码', '代码重构是持续优化代码质量的重要手段。本文介绍重构的原则、技巧和工具，以及如何在不改变功能的前提下改进代码设计。', '<h1>代码重构之道</h1><p>代码重构是持续优化代码质量的重要手段。本文介绍重构的原则、技巧和工具，以及如何在不改变功能的前提下改进代码设计。</p>', 'http://127.0.0.1:9000/article/cover/81.jpg', 1, '1', '1', '1', '0', '1', 1780, 112, 23, 45, '2026-03-15 14:20:00', '2026-03-13 10:30:00'),
(82, 81, 'HTTP/3协议详解', '新一代HTTP协议的革新与实践', 'HTTP/3基于QUIC协议，带来更快的连接建立和更好的性能。本文详细介绍HTTP/3的核心特性、工作原理以及如何应用。', '<h1>HTTP/3协议详解</h1><p>HTTP/3基于QUIC协议，带来更快的连接建立和更好的性能。本文详细介绍HTTP/3的核心特性、工作原理以及如何应用。</p>', 'http://127.0.0.1:9000/article/cover/82.jpg', 6, '1', '2', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-03-14 14:45:00'),
(83, 82, '项目管理实战经验', '从程序员到项目经理的转型之路', '项目管理是技术人向管理岗转型的常见路径。本文分享项目管理的核心理念、方法论以及实践中遇到的问题和解决方案。', '<h1>项目管理实战经验</h1><p>项目管理是技术人向管理岗转型的常见路径。本文分享项目管理的核心理念、方法论以及实践中遇到的问题和解决方案。</p>', 'http://127.0.0.1:9000/article/cover/83.jpg', 8, '2', '2', '1', '0', '1', 1230, 89, 18, 32, '2026-03-16 09:15:00', '2026-03-15 11:20:00'),
(84, 83, 'Elasticsearch入门与实践', '全文搜索引擎的安装、配置和应用', 'Elasticsearch是强大的全文搜索引擎。本文介绍Elasticsearch的核心概念、安装配置、索引管理、搜索语法以及实战应用。', '<h1>Elasticsearch入门与实践</h1><p>Elasticsearch是强大的全文搜索引擎。本文介绍Elasticsearch的核心概念、安装配置、索引管理、搜索语法以及实战应用。</p>', 'http://127.0.0.1:9000/article/cover/84.jpg', 4, '1', '1', '1', '0', '1', 1450, 92, 19, 38, '2026-03-17 10:30:00', '2026-03-15 15:30:00'),
(85, 84, '移动端适配方案', '响应式设计和移动优先的开发策略', '移动互联网时代，移动端适配至关重要。本文介绍响应式设计、rem布局、视口设置等移动端适配方案。', '<h1>移动端适配方案</h1><p>移动互联网时代，移动端适配至关重要。本文介绍响应式设计、rem布局、视口设置等移动端适配方案。</p>', 'http://127.0.0.1:9000/article/cover/85.jpg', 2, '1', '2', '1', '0', '1', 980, 67, 13, 25, '2026-03-18 13:45:00', '2026-03-16 09:20:00'),
(86, 85, 'GraphQL入门指南', 'API查询语言的新选择', 'GraphQL提供更灵活的API查询方式。本文介绍GraphQL的核心概念、类型系统、查询语法，以及与RESTful API的对比。', '<h1>GraphQL入门指南</h1><p>GraphQL提供更灵活的API查询方式。本文介绍GraphQL的核心概念、类型系统、查询语法，以及与RESTful API的对比。</p>', 'http://127.0.0.1:9000/article/cover/86.jpg', 3, '1', '1', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-03-16 13:40:00'),
(87, 86, '程序员理财入门', '技术人的财务规划指南', '除了技术，理财也是程序员需要关注的话题。本文分享基本的理财知识、投资工具选择和风险管理策略。', '<h1>程序员理财入门</h1><p>除了技术，理财也是程序员需要关注的话题。本文分享基本的理财知识、投资工具选择和风险管理策略。</p>', 'http://127.0.0.1:9000/article/cover/87.jpg', 8, '2', '2', '1', '1', '1', 2340, 178, 34, 56, '2026-03-19 15:20:00', '2026-03-17 10:15:00'),
(88, 87, 'WebAssembly实战', '高性能Web应用的新选择', 'WebAssembly为Web应用带来接近原生的性能。本文介绍WebAssembly的基本概念、开发流程以及在实际项目中的应用。', '<h1>WebAssembly实战</h1><p>WebAssembly为Web应用带来接近原生的性能。本文介绍WebAssembly的基本概念、开发流程以及在实际项目中的应用。</p>', 'http://127.0.0.1:9000/article/cover/88.jpg', 2, '1', '1', '1', '0', '1', 890, 56, 11, 21, '2026-03-20 09:30:00', '2026-03-17 14:30:00'),
(89, 88, 'DevOps实践指南', '构建自动化CI/CD流水线', 'DevOps强调开发与运维的协作。本文介绍CI/CD的概念、常用工具（Jenkins、GitLab CI等）以及流水线设计的最佳实践。', '<h1>DevOps实践指南</h1><p>DevOps强调开发与运维的协作。本文介绍CI/CD的概念、常用工具（Jenkins、GitLab CI等）以及流水线设计的最佳实践。</p>', 'http://127.0.0.1:9000/article/cover/89.jpg', 3, '1', '2', '1', '0', '1', 1560, 98, 20, 41, '2026-03-21 11:15:00', '2026-03-18 09:45:00'),
(90, 89, '机器学习实战项目', '从零构建一个推荐系统', '理论结合实践是最好的学习方式。本文通过构建一个简单的推荐系统，讲解机器学习项目的完整流程，包括数据处理、模型训练和评估。', '<h1>机器学习实战项目</h1><p>理论结合实践是最好的学习方式。本文通过构建一个简单的推荐系统，讲解机器学习项目的完整流程，包括数据处理、模型训练和评估。</p>', 'http://127.0.0.1:9000/article/cover/90.jpg', 1, '1', '1', '1', '0', '1', 1890, 123, 26, 49, '2026-03-22 14:40:00', '2026-03-18 13:20:00'),
(91, 90, '分布式系统理论基础', 'CAP、BASE、一致性协议等核心概念解析', '分布式系统是现代互联网应用的基石。本文深入浅出地讲解分布式系统的核心理论，包括CAP定理、BASE理论、Paxos和Raft协议等。', '<h1>分布式系统理论基础</h1><p>分布式系统是现代互联网应用的基石。本文深入浅出地讲解分布式系统的核心理论，包括CAP定理、BASE理论、Paxos和Raft协议等。</p>', 'http://127.0.0.1:9000/article/cover/91.jpg', 3, '1', '2', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-03-19 10:30:00'),
(92, 91, 'VSCode插件开发', '打造自己的IDE工具', 'VSCode是流行的代码编辑器。本文介绍如何开发VSCode插件，包括API使用、调试发布等，让你打造个性化开发工具。', '<h1>VSCode插件开发</h1><p>VSCode是流行的代码编辑器。本文介绍如何开发VSCode插件，包括API使用、调试发布等，让你打造个性化开发工具。</p>', 'http://127.0.0.1:9000/article/cover/92.jpg', 2, '1', '1', '1', '0', '1', 780, 45, 9, 18, '2026-03-23 10:20:00', '2026-03-19 14:15:00');

-- 继续生成article表测试数据，id从93开始
INSERT INTO `article` (`id`, `user_id`, `title`, `summary`, `content`, `content_html`, `cover_image`, `category_id`, `type`, `format`, `status`, `visibility`, `is_original`, `view_count`, `like_count`, `comment_count`, `collect_count`, `published_at`, `created_at`) VALUES
(93, 92, '开源项目维护指南', '如何维护一个成功的开源项目', '开源项目不仅需要写代码，还需要社区运营、文档维护、Issue管理等。本文分享维护开源项目的经验和最佳实践。', '<h1>开源项目维护指南</h1><p>开源项目不仅需要写代码，还需要社区运营、文档维护、Issue管理等。本文分享维护开源项目的经验和最佳实践。</p>', 'http://127.0.0.1:9000/article/cover/93.jpg', 8, '1', '2', '1', '0', '1', 1120, 78, 16, 32, '2026-03-24 13:30:00', '2026-03-20 09:45:00'),
(94, 93, '微服务监控体系搭建', 'Prometheus+Grafana实现全方位监控', '微服务架构需要完善的监控体系。本文介绍如何使用Prometheus收集指标，Grafana可视化展示，以及告警配置的最佳实践。', '<h1>微服务监控体系搭建</h1><p>微服务架构需要完善的监控体系。本文介绍如何使用Prometheus收集指标，Grafana可视化展示，以及告警配置的最佳实践。</p>', 'http://127.0.0.1:9000/article/cover/94.jpg', 3, '1', '1', '1', '0', '1', 1340, 89, 18, 37, '2026-03-25 09:15:00', '2026-03-20 14:20:00'),
(95, 94, '程序员英语学习之路', '提升技术英语能力的实用方法', '英语是程序员的重要技能。本文分享如何高效学习技术英语，包括阅读文档、观看技术视频、参与国际社区等经验。', '<h1>程序员英语学习之路</h1><p>英语是程序员的重要技能。本文分享如何高效学习技术英语，包括阅读文档、观看技术视频、参与国际社区等经验。</p>', 'http://127.0.0.1:9000/article/cover/95.jpg', 8, '2', '2', '1', '0', '1', 2340, 156, 32, 67, '2026-03-26 11:40:00', '2026-03-21 10:30:00'),
(96, 95, 'Serverless架构实战', '无服务器架构的应用场景和最佳实践', 'Serverless让开发者更专注于业务逻辑。本文介绍Serverless的核心概念、主流平台对比，以及实际项目中的应用案例。', '<h1>Serverless架构实战</h1><p>Serverless让开发者更专注于业务逻辑。本文介绍Serverless的核心概念、主流平台对比，以及实际项目中的应用案例。</p>', 'http://127.0.0.1:9000/article/cover/96.jpg', 3, '1', '1', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-03-21 15:45:00'),
(97, 96, '前端安全防范指南', '常见Web前端安全漏洞及防御措施', '前端安全同样不容忽视。本文介绍XSS、CSRF、点击劫持等前端安全威胁，并给出实用的防御方案。', '<h1>前端安全防范指南</h1><p>前端安全同样不容忽视。本文介绍XSS、CSRF、点击劫持等前端安全威胁，并给出实用的防御方案。</p>', 'http://127.0.0.1:9000/article/cover/97.jpg', 6, '1', '2', '1', '0', '1', 890, 67, 13, 25, '2026-03-27 14:20:00', '2026-03-22 09:15:00'),
(98, 97, 'Go语言并发编程', 'Goroutine和Channel的实战应用', 'Go语言的并发模型简单而强大。本文通过实际案例，讲解Goroutine的使用、Channel的通信模式，以及常见的并发模式。', '<h1>Go语言并发编程</h1><p>Go语言的并发模型简单而强大。本文通过实际案例，讲解Goroutine的使用、Channel的通信模式，以及常见的并发模式。</p>', 'http://127.0.0.1:9000/article/cover/98.jpg', 1, '1', '1', '1', '0', '1', 1780, 112, 23, 45, '2026-03-28 10:30:00', '2026-03-22 13:40:00'),
(99, 98, '技术文档写作指南', '写出高质量技术文档的技巧', '技术文档是开发者的重要输出。本文分享技术文档的写作原则、组织结构、语言表达技巧，以及如何让文档更易读。', '<h1>技术文档写作指南</h1><p>技术文档是开发者的重要输出。本文分享技术文档的写作原则、组织结构、语言表达技巧，以及如何让文档更易读。</p>', 'http://127.0.0.1:9000/article/cover/99.jpg', 8, '2', '2', '1', '0', '1', 1450, 98, 19, 34, '2026-03-29 15:45:00', '2026-03-23 10:20:00'),
(100, 99, 'Kubernetes入门到精通', '容器编排平台的学习路径和实践', 'Kubernetes已成为容器编排的事实标准。本文提供K8s的学习路径，从基础概念到实际部署，帮助读者掌握这一重要技能。', '<h1>Kubernetes入门到精通</h1><p>Kubernetes已成为容器编排的事实标准。本文提供K8s的学习路径，从基础概念到实际部署，帮助读者掌握这一重要技能。</p>', 'http://127.0.0.1:9000/article/cover/100.jpg', 3, '1', '1', '1', '0', '1', 2560, 178, 38, 72, '2026-03-30 09:20:00', '2026-03-23 15:30:00'),
(101, 100, '程序员副业探索', '技术人如何开展副业增加收入', '程序员有天然的优势开展副业。本文分享常见的程序员副业方向，包括接外包、做产品、写博客、知识付费等经验。', '<h1>程序员副业探索</h1><p>程序员有天然的优势开展副业。本文分享常见的程序员副业方向，包括接外包、做产品、写博客、知识付费等经验。</p>', 'http://127.0.0.1:9000/article/cover/101.jpg', 8, '2', '2', '1', '0', '1', 3450, 234, 45, 89, '2026-03-31 13:15:00', '2026-03-24 09:30:00'),
(102, 101, 'Node.js性能优化', '提升Node.js应用性能的实用技巧', 'Node.js广泛应用于后端开发。本文从代码层面、架构层面、工具层面分享Node.js性能优化的实战经验。', '<h1>Node.js性能优化</h1><p>Node.js广泛应用于后端开发。本文从代码层面、架构层面、工具层面分享Node.js性能优化的实战经验。</p>', 'http://127.0.0.1:9000/article/cover/102.jpg', 3, '1', '1', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-03-24 14:15:00'),
(103, 102, 'Flutter跨平台开发', '一套代码搞定iOS和Android', 'Flutter是Google的UI框架，可构建原生编译的跨平台应用。本文介绍Flutter的核心概念、开发环境和实战案例。', '<h1>Flutter跨平台开发</h1><p>Flutter是Google的UI框架，可构建原生编译的跨平台应用。本文介绍Flutter的核心概念、开发环境和实战案例。</p>', 'http://127.0.0.1:9000/article/cover/103.jpg', 9, '1', '2', '1', '0', '1', 1230, 86, 17, 33, '2026-04-01 10:40:00', '2026-03-25 10:45:00'),
(104, 103, '团队代码规范制定', '如何建立和执行有效的代码规范', '代码规范提升团队协作效率。本文分享如何制定合理的代码规范、如何落地执行，以及常用工具的配置使用。', '<h1>团队代码规范制定</h1><p>代码规范提升团队协作效率。本文分享如何制定合理的代码规范、如何落地执行，以及常用工具的配置使用。</p>', 'http://127.0.0.1:9000/article/cover/104.jpg', 8, '2', '1', '1', '0', '1', 980, 67, 14, 26, '2026-04-02 14:30:00', '2026-03-25 14:20:00'),
(105, 104, 'PostgreSQL实战指南', '开源数据库的高级特性和优化技巧', 'PostgreSQL功能强大且开源。本文介绍PostgreSQL的安装配置、高级特性、性能优化以及常见问题解决方案。', '<h1>PostgreSQL实战指南</h1><p>PostgreSQL功能强大且开源。本文介绍PostgreSQL的安装配置、高级特性、性能优化以及常见问题解决方案。</p>', 'http://127.0.0.1:9000/article/cover/105.jpg', 4, '1', '1', '1', '0', '1', 1670, 108, 22, 44, '2026-04-03 09:25:00', '2026-03-26 09:15:00'),
(106, 105, '程序员职业规划', '技术人的成长路径和发展方向', '程序员如何规划职业生涯？本文分享技术路线、管理路线、创业路线的选择，以及每个阶段需要准备的能力。', '<h1>程序员职业规划</h1><p>程序员如何规划职业生涯？本文分享技术路线、管理路线、创业路线的选择，以及每个阶段需要准备的能力。</p>', 'http://127.0.0.1:9000/article/cover/106.jpg', 8, '2', '2', '1', '0', '1', 2890, 190, 42, 78, '2026-04-04 11:50:00', '2026-03-26 15:30:00'),
(107, 106, 'MongoDB从入门到实战', 'NoSQL数据库的应用场景和最佳实践', 'MongoDB是流行的文档型NoSQL数据库。本文介绍MongoDB的数据模型、查询语法、索引设计以及实际应用案例。', '<h1>MongoDB从入门到实战</h1><p>MongoDB是流行的文档型NoSQL数据库。本文介绍MongoDB的数据模型、查询语法、索引设计以及实际应用案例。</p>', 'http://127.0.0.1:9000/article/cover/107.jpg', 4, '1', '1', '3', '0', '1', 0, 0, 0, 0, NULL, '2026-03-27 10:20:00'),
(108, 107, 'Web组件化开发', '自定义元素和Shadow DOM实战', 'Web Components提供了创建可复用组件的标准方式。本文介绍自定义元素、Shadow DOM、HTML模板的使用方法。', '<h1>Web组件化开发</h1><p>Web Components提供了创建可复用组件的标准方式。本文介绍自定义元素、Shadow DOM、HTML模板的使用方法。</p>', 'http://127.0.0.1:9000/article/cover/108.jpg', 2, '1', '2', '1', '0', '1', 780, 52, 10, 19, '2026-04-05 13:20:00', '2026-03-27 13:45:00'),
(109, 108, '数据中台建设实践', '企业数据中台的架构设计和实施经验', '数据中台是数字化转型的关键。本文分享数据中台的概念、架构设计、技术选型以及实施过程中遇到的挑战和解决方案。', '<h1>数据中台建设实践</h1><p>数据中台是数字化转型的关键。本文分享数据中台的概念、架构设计、技术选型以及实施过程中遇到的挑战和解决方案。</p>', 'http://127.0.0.1:9000/article/cover/109.jpg', 9, '1', '1', '1', '0', '1', 1450, 94, 19, 37, '2026-04-06 15:40:00', '2026-03-28 09:30:00'),
(110, 109, '程序员阅读书单', '技术人必读的经典书籍推荐', '阅读是提升技术视野的重要途径。本文推荐程序员各个阶段值得阅读的经典书籍，涵盖编程、架构、软技能等方向。', '<h1>程序员阅读书单</h1><p>阅读是提升技术视野的重要途径。本文推荐程序员各个阶段值得阅读的经典书籍，涵盖编程、架构、软技能等方向。</p>', 'http://127.0.0.1:9000/article/cover/110.jpg', 8, '2', '2', '1', '0', '1', 3120, 210, 45, 89, '2026-04-07 09:35:00', '2026-03-28 14:15:00'),
(111, 110, 'Nginx配置详解', '高性能Web服务器的配置和优化', 'Nginx是高性能的Web服务器和反向代理服务器。本文详细介绍Nginx的配置指令、常见场景配置以及性能调优方法。', '<h1>Nginx配置详解</h1><p>Nginx是高性能的Web服务器和反向代理服务器。本文详细介绍Nginx的配置指令、常见场景配置以及性能调优方法。</p>', 'http://127.0.0.1:9000/article/cover/111.jpg', 3, '1', '1', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-03-29 10:45:00'),
(112, 111, '微信小程序开发实战', '从入门到上线的完整流程', '微信小程序是重要的移动端应用形态。本文从小程序注册、开发环境搭建、组件使用、API调用到上线发布，提供完整指南。', '<h1>微信小程序开发实战</h1><p>微信小程序是重要的移动端应用形态。本文从小程序注册、开发环境搭建、组件使用、API调用到上线发布，提供完整指南。</p>', 'http://127.0.0.1:9000/article/cover/112.jpg', 9, '1', '2', '1', '0', '1', 2340, 156, 32, 61, '2026-04-08 11:20:00', '2026-03-29 13:30:00'),
(113, 112, '技术面试官视角', '从面试官角度看技术面试的准备要点', '了解面试官的思维有助于更好准备面试。本文分享作为技术面试官的考察重点、评判标准和常见误区。', '<h1>技术面试官视角</h1><p>了解面试官的思维有助于更好准备面试。本文分享作为技术面试官的考察重点、评判标准和常见误区。</p>', 'http://127.0.0.1:9000/article/cover/113.jpg', 8, '2', '1', '1', '0', '1', 1780, 121, 25, 48, '2026-04-09 14:15:00', '2026-03-30 09:20:00'),
(114, 113, 'GraphQL+React实战', '构建现代化的数据获取方案', 'GraphQL与React结合带来出色的开发体验。本文通过实战项目，讲解如何在React应用中使用GraphQL进行数据管理。', '<h1>GraphQL+React实战</h1><p>GraphQL与React结合带来出色的开发体验。本文通过实战项目，讲解如何在React应用中使用GraphQL进行数据管理。</p>', 'http://127.0.0.1:9000/article/cover/114.jpg', 2, '1', '1', '1', '0', '1', 1290, 86, 17, 33, '2026-04-10 10:30:00', '2026-03-30 14:45:00'),
(115, 114, '分布式事务解决方案', '微服务架构下的事务一致性实践', '分布式事务是微服务架构的难点。本文介绍分布式事务的理论基础，以及Seata、TCC、Saga等实现方案。', '<h1>分布式事务解决方案</h1><p>分布式事务是微服务架构的难点。本文介绍分布式事务的理论基础，以及Seata、TCC、Saga等实现方案。</p>', 'http://127.0.0.1:9000/article/cover/115.jpg', 3, '1', '2', '1', '0', '1', 1560, 101, 21, 41, '2026-04-11 15:45:00', '2026-03-31 10:15:00'),
(116, 115, '程序员健康饮食指南', '久坐族的饮食调理建议', '健康饮食对程序员尤为重要。本文分享适合程序员的饮食原则、营养搭配和简单易做的健康食谱。', '<h1>程序员健康饮食指南</h1><p>健康饮食对程序员尤为重要。本文分享适合程序员的饮食原则、营养搭配和简单易做的健康食谱。</p>', 'http://127.0.0.1:9000/article/cover/116.jpg', 7, '2', '2', '3', '0', '1', 0, 0, 0, 0, NULL, '2026-03-31 15:30:00'),
(117, 116, 'Rust语言入门', '安全、并发、实用的现代编程语言', 'Rust以其安全性和性能受到关注。本文介绍Rust的核心概念、所有权系统、借用检查器，以及入门学习路径。', '<h1>Rust语言入门</h1><p>Rust以其安全性和性能受到关注。本文介绍Rust的核心概念、所有权系统、借用检查器，以及入门学习路径。</p>', 'http://127.0.0.1:9000/article/cover/117.jpg', 1, '1', '1', '1', '0', '1', 890, 59, 12, 23, '2026-04-12 09:20:00', '2026-04-01 09:45:00'),
(118, 117, '敏捷开发实战', 'Scrum和Kanban在团队中的应用', '敏捷开发已成为主流开发模式。本文分享敏捷开发的核心理念、Scrum框架实施经验，以及Kanban在团队中的应用。', '<h1>敏捷开发实战</h1><p>敏捷开发已成为主流开发模式。本文分享敏捷开发的核心理念、Scrum框架实施经验，以及Kanban在团队中的应用。</p>', 'http://127.0.0.1:9000/article/cover/118.jpg', 8, '2', '2', '1', '0', '1', 1340, 89, 18, 35, '2026-04-13 13:30:00', '2026-04-01 14:20:00'),
(119, 118, 'Chrome开发者工具使用技巧', '前端调试和性能分析的利器', 'Chrome DevTools是前端开发必备工具。本文介绍DevTools的进阶使用技巧，包括调试、性能分析、网络监控等。', '<h1>Chrome开发者工具使用技巧</h1><p>Chrome DevTools是前端开发必备工具。本文介绍DevTools的进阶使用技巧，包括调试、性能分析、网络监控等。</p>', 'http://127.0.0.1:9000/article/cover/119.jpg', 2, '1', '1', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-04-02 10:30:00'),
(120, 119, 'API网关选型与实践', 'Kong、APISIX、Spring Cloud Gateway对比', 'API网关是微服务架构的重要组件。本文对比主流API网关的特性、性能、生态，并给出选型建议和最佳实践。', '<h1>API网关选型与实践</h1><p>API网关是微服务架构的重要组件。本文对比主流API网关的特性、性能、生态，并给出选型建议和最佳实践。</p>', 'http://127.0.0.1:9000/article/cover/120.jpg', 3, '1', '1', '1', '0', '1', 1120, 74, 15, 29, '2026-04-14 11:15:00', '2026-04-02 13:45:00'),
(121, 120, '程序员时间管理', '高效能程序员的日程规划方法', '时间管理提升工作效率。本文分享番茄工作法、GTD等时间管理方法，以及如何在多任务并行中保持专注。', '<h1>程序员时间管理</h1><p>时间管理提升工作效率。本文分享番茄工作法、GTD等时间管理方法，以及如何在多任务并行中保持专注。</p>', 'http://127.0.0.1:9000/article/cover/121.jpg', 8, '2', '2', '1', '0', '1', 1890, 128, 26, 51, '2026-04-15 14:40:00', '2026-04-03 09:30:00'),
(122, 121, 'GraphQL进阶', 'Schema设计、性能优化和最佳实践', 'GraphQL在生产环境的应用需要更多考量。本文分享Schema设计原则、N+1问题解决、缓存策略等进阶话题。', '<h1>GraphQL进阶</h1><p>GraphQL在生产环境的应用需要更多考量。本文分享Schema设计原则、N+1问题解决、缓存策略等进阶话题。</p>', 'http://127.0.0.1:9000/article/cover/122.jpg', 3, '1', '1', '1', '0', '1', 980, 63, 12, 24, '2026-04-16 09:50:00', '2026-04-03 14:15:00'),
(123, 122, 'WebSocket实战', '实时通信应用的开发指南', 'WebSocket实现全双工通信。本文介绍WebSocket协议、Node.js实现、客户端API，以及聊天室、实时推送等应用场景。', '<h1>WebSocket实战</h1><p>WebSocket实现全双工通信。本文介绍WebSocket协议、Node.js实现、客户端API，以及聊天室、实时推送等应用场景。</p>', 'http://127.0.0.1:9000/article/cover/123.jpg', 2, '1', '2', '1', '0', '1', 1230, 82, 17, 33, '2026-04-17 12:20:00', '2026-04-04 10:20:00'),
(124, 123, '单元测试最佳实践', '编写高质量单元测试的指南', '单元测试保障代码质量。本文介绍单元测试的原则、Mock技巧、测试覆盖率，以及如何在项目中落地测试文化。', '<h1>单元测试最佳实践</h1><p>单元测试保障代码质量。本文介绍单元测试的原则、Mock技巧、测试覆盖率，以及如何在项目中落地测试文化。</p>', 'http://127.0.0.1:9000/article/cover/124.jpg', 1, '1', '1', '3', '0', '1', 0, 0, 0, 0, NULL, '2026-04-04 15:30:00'),
(125, 124, '远程工作指南', '高效远程办公的经验分享', '远程工作成为新常态。本文分享远程工作的工具选择、沟通技巧、时间管理以及如何保持工作与生活平衡。', '<h1>远程工作指南</h1><p>远程工作成为新常态。本文分享远程工作的工具选择、沟通技巧、时间管理以及如何保持工作与生活平衡。</p>', 'http://127.0.0.1:9000/article/cover/125.jpg', 8, '2', '2', '1', '0', '1', 2670, 180, 37, 69, '2026-04-18 15:35:00', '2026-04-05 09:45:00'),
(126, 125, 'Istio服务网格', '云原生时代的服务治理方案', 'Istio提供服务网格的完整解决方案。本文介绍Istio的架构、流量管理、安全策略、可观测性等核心功能。', '<h1>Istio服务网格</h1><p>Istio提供服务网格的完整解决方案。本文介绍Istio的架构、流量管理、安全策略、可观测性等核心功能。</p>', 'http://127.0.0.1:9000/article/cover/126.jpg', 3, '1', '1', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-04-05 13:20:00'),
(127, 126, 'CSS架构设计', '可维护、可扩展的CSS编写方法', 'CSS在大型项目中容易失控。本文介绍BEM、ITCSS、CSS Modules等CSS架构方案，以及如何组织样式代码。', '<h1>CSS架构设计</h1><p>CSS在大型项目中容易失控。本文介绍BEM、ITCSS、CSS Modules等CSS架构方案，以及如何组织样式代码。</p>', 'http://127.0.0.1:9000/article/cover/127.jpg', 2, '1', '1', '1', '0', '1', 890, 58, 11, 22, '2026-04-19 10:25:00', '2026-04-06 10:15:00'),
(128, 127, '持续集成实践', 'Jenkins Pipeline和GitLab CI配置指南', '持续集成是DevOps的核心实践。本文介绍如何搭建CI流水线，包括代码检查、自动化测试、构建部署等环节。', '<h1>持续集成实践</h1><p>持续集成是DevOps的核心实践。本文介绍如何搭建CI流水线，包括代码检查、自动化测试、构建部署等环节。</p>', 'http://127.0.0.1:9000/article/cover/128.jpg', 3, '1', '2', '1', '0', '1', 1450, 94, 19, 37, '2026-04-20 13:45:00', '2026-04-06 14:30:00'),
(129, 128, '程序员沟通技巧', '技术人员如何有效沟通', '沟通能力影响职业发展。本文分享程序员与产品、设计、测试等角色沟通的技巧，以及如何表达技术方案。', '<h1>程序员沟通技巧</h1><p>沟通能力影响职业发展。本文分享程序员与产品、设计、测试等角色沟通的技巧，以及如何表达技术方案。</p>', 'http://127.0.0.1:9000/article/cover/129.jpg', 8, '2', '2', '1', '0', '1', 1560, 104, 21, 41, '2026-04-21 09:30:00', '2026-04-07 09:45:00'),
(130, 129, '电商系统架构设计', '高并发电商平台的技术方案', '电商系统面临高并发挑战。本文分享电商系统的架构设计，包括商品、订单、库存、支付等核心模块的设计思路。', '<h1>电商系统架构设计</h1><p>电商系统面临高并发挑战。本文分享电商系统的架构设计，包括商品、订单、库存、支付等核心模块的设计思路。</p>', 'http://127.0.0.1:9000/article/cover/130.jpg', 9, '1', '1', '1', '0', '1', 2340, 156, 31, 62, '2026-04-22 14:15:00', '2026-04-07 15:20:00'),
(131, 130, '前端构建工具', 'Webpack、Vite、Rollup对比与选型', '构建工具是前端开发的基础。本文对比主流构建工具的特性、性能、生态，帮助读者做出合适的选择。', '<h1>前端构建工具</h1><p>构建工具是前端开发的基础。本文对比主流构建工具的特性、性能、生态，帮助读者做出合适的选择。</p>', 'http://127.0.0.1:9000/article/cover/131.jpg', 2, '1', '1', '3', '0', '1', 0, 0, 0, 0, NULL, '2026-04-08 10:30:00'),
(132, 131, '数据一致性方案', '分布式系统中的数据一致性实践', '数据一致性是分布式系统的难题。本文介绍最终一致性、强一致性、以及基于消息队列的可靠一致性方案。', '<h1>数据一致性方案</h1><p>数据一致性是分布式系统的难题。本文介绍最终一致性、强一致性、以及基于消息队列的可靠一致性方案。</p>', 'http://127.0.0.1:9000/article/cover/132.jpg', 3, '1', '2', '1', '0', '1', 1120, 73, 15, 29, '2026-04-23 11:40:00', '2026-04-08 13:45:00'),
(133, 132, '程序员投资理财', '技术人的资产配置建议', '理财规划是财务自由的基础。本文分享适合程序员的投资理念、资产配置策略和风险控制方法。', '<h1>程序员投资理财</h1><p>理财规划是财务自由的基础。本文分享适合程序员的投资理念、资产配置策略和风险控制方法。</p>', 'http://127.0.0.1:9000/article/cover/133.jpg', 8, '2', '2', '1', '0', '1', 1890, 126, 25, 49, '2026-04-24 15:20:00', '2026-04-09 09:30:00'),
(134, 133, 'GraphQL+TypeScript', '类型安全的API开发方案', 'GraphQL和TypeScript结合提供类型安全的开发体验。本文介绍如何用TypeScript开发GraphQL服务端和客户端。', '<h1>GraphQL+TypeScript</h1><p>GraphQL和TypeScript结合提供类型安全的开发体验。本文介绍如何用TypeScript开发GraphQL服务端和客户端。</p>', 'http://127.0.0.1:9000/article/cover/134.jpg', 2, '1', '1', '1', '0', '1', 890, 59, 11, 22, '2026-04-25 09:45:00', '2026-04-09 14:15:00'),
(135, 134, 'Redis高级特性', 'Redis模块、集群和持久化深入', 'Redis不仅可用作缓存，还提供更多高级特性。本文介绍Redis模块、集群部署、持久化机制和性能调优。', '<h1>Redis高级特性</h1><p>Redis不仅可用作缓存，还提供更多高级特性。本文介绍Redis模块、集群部署、持久化机制和性能调优。</p>', 'http://127.0.0.1:9000/article/cover/135.jpg', 4, '1', '1', '2', '0', '1', 0, 0, 0, 0, NULL, '2026-04-10 10:40:00'),
(136, 135, 'Svelte框架入门', '编译型前端框架的新选择', 'Svelte采用编译时优化，带来更小的包体积和更好的性能。本文介绍Svelte的核心概念、开发体验和适用场景。', '<h1>Svelte框架入门</h1><p>Svelte采用编译时优化，带来更小的包体积和更好的性能。本文介绍Svelte的核心概念、开发体验和适用场景。</p>', 'http://127.0.0.1:9000/article/cover/136.jpg', 2, '1', '2', '1', '0', '1', 670, 44, 8, 16, '2026-04-26 13:15:00', '2026-04-10 13:30:00'),
(137, 136, '技术领导力', '从技术骨干到技术Leader的转型', '技术领导力不仅是管理能力。本文分享如何培养技术视野、团队建设、决策能力，以及技术Leader的职责。', '<h1>技术领导力</h1><p>技术领导力不仅是管理能力。本文分享如何培养技术视野、团队建设、决策能力，以及技术Leader的职责。</p>', 'http://127.0.0.1:9000/article/cover/137.jpg', 8, '2', '1', '1', '0', '1', 2340, 156, 31, 60, '2026-04-27 15:30:00', '2026-04-11 09:45:00'),
(138, 137, 'MQTT协议实战', '物联网通信协议的应用', 'MQTT是物联网场景的轻量级通信协议。本文介绍MQTT协议特性、Broker选型，以及在IoT项目中的应用。', '<h1>MQTT协议实战</h1><p>MQTT是物联网场景的轻量级通信协议。本文介绍MQTT协议特性、Broker选型，以及在IoT项目中的应用。</p>', 'http://127.0.0.1:9000/article/cover/138.jpg', 9, '1', '1', '1', '0', '1', 780, 51, 10, 19, '2026-04-28 10:20:00', '2026-04-11 14:30:00'),
(139, 138, '微服务测试策略', '单元测试、契约测试和端到端测试', '微服务架构带来新的测试挑战。本文介绍微服务的测试金字塔，以及如何实施单元测试、契约测试和端到端测试。', '<h1>微服务测试策略</h1><p>微服务架构带来新的测试挑战。本文介绍微服务的测试金字塔，以及如何实施单元测试、契约测试和端到端测试。</p>', 'http://127.0.0.1:9000/article/cover/139.jpg', 3, '1', '2', '3', '0', '1', 0, 0, 0, 0, NULL, '2026-04-12 10:15:00');


-- 一级兜底分类
INSERT INTO category
(id, parent_id, level, name, slug, icon, description, sort, status)
VALUES
(88, 0, '1', '综合', 'general', 'icon-general', '综合技术与杂谈内容', 99, '1');
-- 第一级分类：技术领域大类
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`, `icon`, `is_nav`) VALUES
(0, 1, '前端开发', 'frontend', '前端开发相关技术', 'icon-code', 1),
(0, 1, '后端开发', 'backend', '后端开发相关技术', 'icon-server', 1),
(0, 1, '移动开发', 'mobile', '移动应用开发', 'icon-phone', 1),
(0, 1, '数据库', 'database', '数据库相关技术', 'icon-database', 1),
(0, 1, '运维部署', 'devops', '运维与部署技术', 'icon-deploy', 1),
(0, 1, '人工智能', 'ai', '人工智能与机器学习', 'icon-ai', 1),
(0, 1, '编程语言', 'languages', '各类编程语言', 'icon-language', 1),
(0, 1, '工具软件', 'tools', '开发工具与软件', 'icon-tool', 1),
(0, 1, '计算机基础', 'computer-basics', '计算机基础理论', 'icon-computer', 0),
(0, 1, '硬件相关', 'hardware', '硬件与嵌入式开发', 'icon-hardware', 1);
-- 第二级分类：前端开发子分类
SET @frontend_id = (SELECT id FROM category WHERE slug = 'frontend');
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`) VALUES
(@frontend_id, 2, 'HTML/CSS', 'html-css', 'HTML和CSS相关技术'),
(@frontend_id, 2, 'JavaScript', 'javascript', 'JavaScript核心与进阶'),
(@frontend_id, 2, 'Vue.js', 'vue', 'Vue.js框架相关'),
(@frontend_id, 2, 'React', 'react', 'React框架相关'),
(@frontend_id, 2, 'Angular', 'angular', 'Angular框架相关'),
(@frontend_id, 2, '前端工程化', 'frontend-engineering', '构建工具、模块化等'),
(@frontend_id, 2, '小程序开发', 'mini-program', '微信小程序等'),
(@frontend_id, 2, 'TypeScript', 'typescript', 'TypeScript语言'),
(@frontend_id, 2, 'Node.js', 'nodejs-frontend', 'Node.js在前端的应用');
-- 第三级分类：JavaScript子分类
SET @javascript_id = (SELECT id FROM category WHERE slug = 'javascript');
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`) VALUES
(@javascript_id, 3, 'ES6+', 'es6-plus', 'ES6及以上新特性'),
(@javascript_id, 3, '异步编程', 'async-programming', 'Promise、async/await等'),
(@javascript_id, 3, '设计模式', 'design-patterns', 'JavaScript设计模式'),
(@javascript_id, 3, 'DOM/BOM', 'dom-bom', 'DOM和BOM操作'),
(@javascript_id, 3, '性能优化', 'performance', 'JavaScript性能优化'),
(@javascript_id, 3, '安全相关', 'security', 'JavaScript安全问题');
-- 第二级分类：后端开发子分类
SET @backend_id = (SELECT id FROM category WHERE slug = 'backend');
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`) VALUES
(@backend_id, 2, 'Java', 'java', 'Java开发相关'),
(@backend_id, 2, 'Spring', 'spring', 'Spring框架生态'),
(@backend_id, 2, 'Python', 'python', 'Python开发相关'),
(@backend_id, 2, 'Go', 'golang', 'Go语言开发'),
(@backend_id, 2, 'PHP', 'php', 'PHP开发相关'),
(@backend_id, 2, '.NET', 'dotnet', '.NET平台开发'),
(@backend_id, 2, 'Rust', 'rust', 'Rust语言开发'),
(@backend_id, 2, '微服务', 'microservices', '微服务架构'),
(@backend_id, 2, 'API设计', 'api-design', 'RESTful、GraphQL等');
-- 第三级分类：Java子分类
SET @java_id = (SELECT id FROM category WHERE slug = 'java');
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`) VALUES
(@java_id, 3, 'Java基础', 'java-basics', 'Java核心语法与特性'),
(@java_id, 3, 'JVM', 'jvm', 'Java虚拟机原理与调优'),
(@java_id, 3, '并发编程', 'concurrent', '多线程与并发'),
(@java_id, 3, '集合框架', 'collections', 'Java集合框架'),
(@java_id, 3, 'IO/NIO', 'io', '输入输出流'),
(@java_id, 3, '网络编程', 'network', 'Socket、HTTP等网络编程');
-- 第三级分类：Spring子分类
SET @spring_id = (SELECT id FROM category WHERE slug = 'spring');
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`) VALUES
(@spring_id, 3, 'Spring Boot', 'spring-boot', 'Spring Boot框架'),
(@spring_id, 3, 'Spring Cloud', 'spring-cloud', 'Spring Cloud微服务'),
(@spring_id, 3, 'Spring Security', 'spring-security', 'Spring Security安全框架'),
(@spring_id, 3, 'Spring Data', 'spring-data', 'Spring Data数据访问'),
(@spring_id, 3, 'Spring MVC', 'spring-mvc', 'Spring MVC框架'),
(@spring_id, 3, 'Spring AOP', 'spring-aop', 'Spring面向切面编程');
-- 第二级分类：移动开发子分类
SET @mobile_id = (SELECT id FROM category WHERE slug = 'mobile');
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`) VALUES
(@mobile_id, 2, 'Android', 'android', 'Android应用开发'),
(@mobile_id, 2, 'iOS', 'ios', 'iOS应用开发'),
(@mobile_id, 2, 'Flutter', 'flutter', 'Flutter跨平台开发'),
(@mobile_id, 2, 'React Native', 'react-native', 'React Native开发'),
(@mobile_id, 2, '跨平台开发', 'cross-platform', '其他跨平台方案'),
(@mobile_id, 2, '移动安全', 'mobile-security', '移动应用安全');
-- 第二级分类：数据库子分类
SET @database_id = (SELECT id FROM category WHERE slug = 'database');
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`) VALUES
(@database_id, 2, 'MySQL', 'mysql', 'MySQL数据库'),
(@database_id, 2, 'Redis', 'redis', 'Redis内存数据库'),
(@database_id, 2, 'MongoDB', 'mongodb', 'MongoDB文档数据库'),
(@database_id, 2, 'PostgreSQL', 'postgresql', 'PostgreSQL数据库'),
(@database_id, 2, 'Oracle', 'oracle', 'Oracle数据库'),
(@database_id, 2, 'SQL Server', 'sqlserver', 'SQL Server数据库'),
(@database_id, 2, '数据库设计', 'database-design', '数据库设计与优化'),
(@database_id, 2, 'SQL语言', 'sql-language', 'SQL语法与技巧');
-- 第二级分类：运维部署子分类
SET @devops_id = (SELECT id FROM category WHERE slug = 'devops');
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`) VALUES
(@devops_id, 2, 'Linux', 'linux', 'Linux操作系统'),
(@devops_id, 2, 'Docker', 'docker', 'Docker容器技术'),
(@devops_id, 2, 'Kubernetes', 'kubernetes', 'Kubernetes容器编排'),
(@devops_id, 2, 'CI/CD', 'ci-cd', '持续集成与部署'),
(@devops_id, 2, 'Nginx', 'nginx', 'Nginx服务器'),
(@devops_id, 2, '监控告警', 'monitoring', '系统监控与告警'),
(@devops_id, 2, '自动化运维', 'automation', '自动化运维工具');
-- 第二级分类：人工智能子分类
SET @ai_id = (SELECT id FROM category WHERE slug = 'ai');
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`) VALUES
(@ai_id, 2, '机器学习', 'machine-learning', '机器学习算法'),
(@ai_id, 2, '深度学习', 'deep-learning', '深度学习与神经网络'),
(@ai_id, 2, '自然语言处理', 'nlp', '自然语言处理'),
(@ai_id, 2, '计算机视觉', 'computer-vision', '图像识别与处理'),
(@ai_id, 2, '推荐系统', 'recommendation', '推荐算法与系统'),
(@ai_id, 2, 'TensorFlow', 'tensorflow', 'TensorFlow框架'),
(@ai_id, 2, 'PyTorch', 'pytorch', 'PyTorch框架');
-- 第二级分类：编程语言子分类
SET @languages_id = (SELECT id FROM category WHERE slug = 'languages');
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`) VALUES
(@languages_id, 2, 'C/C++', 'c-cpp', 'C和C++语言'),
(@languages_id, 2, 'C#', 'csharp', 'C#语言'),
(@languages_id, 2, 'Ruby', 'ruby', 'Ruby语言'),
(@languages_id, 2, 'Swift', 'swift', 'Swift语言'),
(@languages_id, 2, 'Kotlin', 'kotlin', 'Kotlin语言'),
(@languages_id, 2, 'Scala', 'scala', 'Scala语言'),
(@languages_id, 2, 'Shell', 'shell', 'Shell脚本语言');
-- 第二级分类：硬件相关子分类
SET @hardware_id = (SELECT id FROM category WHERE slug = 'hardware');
INSERT INTO `category` (`parent_id`, `level`, `name`, `slug`, `description`) VALUES
(@hardware_id, 2, '嵌入式开发', 'embedded', '嵌入式系统开发'),
(@hardware_id, 2, '物联网', 'iot', '物联网技术'),
(@hardware_id, 2, '单片机', 'microcontroller', '单片机开发'),
(@hardware_id, 2, 'ARM开发', 'arm', 'ARM架构开发'),
(@hardware_id, 2, 'FPGA', 'fpga', 'FPGA开发'),
(@hardware_id, 2, '硬件安全', 'hardware-security', '硬件安全技术');
-- 更新顶级分类的排序
UPDATE `category` SET `sort` = 1 WHERE `slug` = 'frontend';
UPDATE `category` SET `sort` = 2 WHERE `slug` = 'backend';
UPDATE `category` SET `sort` = 3 WHERE `slug` = 'mobile';
UPDATE `category` SET `sort` = 4 WHERE `slug` = 'database';
UPDATE `category` SET `sort` = 5 WHERE `slug` = 'devops';
UPDATE `category` SET `sort` = 6 WHERE `slug` = 'ai';
UPDATE `category` SET `sort` = 7 WHERE `slug` = 'languages';
UPDATE `category` SET `sort` = 8 WHERE `slug` = 'tools';
UPDATE `category` SET `sort` = 9 WHERE `slug` = 'hardware';

INSERT INTO `article_tag` (`name`, `slug`, `description`, `color`) VALUES
('Vue.js', 'vue', '渐进式JavaScript框架', '#42b883'),
('React', 'react', '用于构建用户界面的JavaScript库', '#61dafb'),
('Spring Boot', 'springboot', 'Java快速开发框架', '#6db33f'),
('Python', 'python', '高级编程语言', '#3776ab'),
('Docker', 'docker', '容器化平台', '#2496ed'),
('Redis', 'redis', '内存数据库', '#d82c20');

-- 生成article_tag_relation表测试数据，id从149开始
INSERT INTO `article_tag_relation` (`id`, `article_id`, `tag_id`, `created_at`, `hit_count`, `density`, `score`, `source`, `confirmed`) VALUES
(337, 53, 61, '2026-02-06 10:15:23', 5, 0.0325, 8.50, '1', '1'),
(150, 53, 62, '2026-02-06 10:15:23', 3, 0.0210, 7.50, '1', '1'),
(151, 53, 65, '2026-02-06 10:15:23', 2, 0.0150, 6.00, '2', '0'),
(152, 53, 78, '2026-02-06 10:15:23', 4, 0.0280, 7.80, '1', '1'),
(153, 54, 61, '2026-02-08 09:30:45', 8, 0.0450, 9.20, '1', '1'),
(154, 54, 62, '2026-02-08 09:30:45', 6, 0.0380, 8.90, '1', '1'),
(155, 54, 63, '2026-02-08 09:30:45', 4, 0.0250, 7.50, '1', '0'),
(156, 55, 4, '2026-02-10 11:20:30', 12, 0.0620, 9.50, '1', '1'),
(157, 55, 78, '2026-02-10 11:20:30', 8, 0.0410, 8.80, '1', '1'),
(158, 55, 77, '2026-02-10 11:20:30', 3, 0.0180, 6.50, '2', '0'),
(159, 56, 50, '2026-02-12 13:45:15', 10, 0.0530, 9.00, '1', '1'),
(160, 56, 51, '2026-02-12 13:45:15', 7, 0.0390, 8.50, '1', '1'),
(161, 56, 7, '2026-02-12 13:45:15', 5, 0.0280, 7.80, '1', '1'),
(162, 56, 52, '2026-02-12 13:45:15', 2, 0.0120, 5.50, '2', '0'),
(163, 57, 19, '2026-02-15 10:00:42', 9, 0.0480, 8.70, '1', '1'),
(164, 57, 78, '2026-02-15 10:00:42', 6, 0.0340, 8.20, '1', '1'),
(165, 57, 77, '2026-02-15 10:00:42', 3, 0.0160, 6.30, '1', '0'),
(166, 58, 54, '2026-02-17 14:30:18', 7, 0.0370, 8.40, '1', '1'),
(167, 58, 77, '2026-02-17 14:30:18', 4, 0.0220, 7.20, '1', '1'),
(168, 58, 80, '2026-02-17 14:30:18', 2, 0.0110, 5.80, '2', '0'),
(169, 59, 29, '2026-02-19 09:15:37', 11, 0.0580, 9.30, '1', '1'),
(170, 59, 77, '2026-02-19 09:15:37', 5, 0.0260, 7.60, '1', '1'),
(171, 59, 75, '2026-02-19 09:15:37', 3, 0.0150, 6.20, '1', '0'),
(172, 60, 5, '2026-02-21 16:45:22', 8, 0.0420, 8.90, '1', '1'),
(173, 60, 30, '2026-02-21 16:45:22', 6, 0.0310, 8.10, '1', '1'),
(174, 60, 27, '2026-02-21 16:45:22', 2, 0.0090, 5.00, '2', '0'),
(175, 61, 1, '2026-02-20 11:30:55', 9, 0.0470, 9.10, '1', '1'),
(176, 61, 2, '2026-02-20 11:30:55', 7, 0.0360, 8.30, '1', '1'),
(177, 61, 33, '2026-02-20 11:30:55', 4, 0.0210, 7.00, '1', '0'),
(178, 62, 3, '2026-02-22 13:20:11', 6, 0.0330, 8.00, '1', '1'),
(179, 62, 8, '2026-02-22 13:20:11', 5, 0.0270, 7.50, '1', '1'),
(180, 62, 78, '2026-02-22 13:20:11', 3, 0.0150, 6.40, '1', '0'),
(181, 63, 25, '2026-02-24 10:45:33', 4, 0.0220, 6.80, '1', '1'),
(182, 63, 26, '2026-02-24 10:45:33', 3, 0.0180, 6.20, '1', '1'),
(183, 63, 70, '2026-02-24 10:45:33', 2, 0.0100, 5.50, '2', '0'),
(184, 64, 79, '2026-02-25 15:30:47', 7, 0.0360, 8.20, '1', '1'),
(185, 64, 80, '2026-02-25 15:30:47', 5, 0.0250, 7.30, '1', '1'),
(186, 64, 82, '2026-02-25 15:30:47', 4, 0.0200, 6.90, '1', '0'),
(187, 65, 1, '2026-02-26 09:20:14', 10, 0.0520, 9.40, '1', '1'),
(188, 65, 20, '2026-02-26 09:20:14', 6, 0.0300, 8.00, '1', '1'),
(189, 65, 78, '2026-02-26 09:20:14', 4, 0.0190, 6.80, '2', '0'),
(190, 66, 6, '2026-02-27 14:15:28', 8, 0.0410, 8.70, '1', '1'),
(191, 66, 19, '2026-02-27 14:15:28', 5, 0.0260, 7.60, '1', '1'),
(192, 66, 78, '2026-02-27 14:15:28', 3, 0.0140, 6.10, '1', '0'),
(193, 67, 50, '2026-02-28 16:30:52', 6, 0.0320, 8.10, '1', '1'),
(194, 67, 27, '2026-02-28 16:30:52', 4, 0.0210, 7.20, '1', '1'),
(195, 67, 60, '2026-02-28 16:30:52', 2, 0.0090, 5.20, '2', '0'),
(196, 68, 19, '2026-03-01 10:45:39', 9, 0.0480, 9.00, '1', '1'),
(197, 68, 78, '2026-03-01 10:45:39', 7, 0.0370, 8.50, '1', '1'),
(198, 68, 77, '2026-03-01 10:45:39', 3, 0.0160, 6.40, '1', '0'),
(199, 69, 2, '2026-03-02 11:30:05', 11, 0.0580, 9.60, '1', '1'),
(200, 69, 20, '2026-03-02 11:30:05', 8, 0.0420, 8.90, '1', '1'),
(201, 69, 32, '2026-03-02 11:30:05', 4, 0.0200, 7.10, '1', '0'),
(202, 70, 70, '2026-03-03 14:20:17', 15, 0.0750, 9.80, '1', '1'),
(203, 70, 71, '2026-03-03 14:20:17', 12, 0.0620, 9.40, '1', '1'),
(204, 70, 72, '2026-03-03 14:20:17', 10, 0.0510, 9.00, '1', '1'),
(205, 70, 74, '2026-03-03 14:20:17', 5, 0.0240, 7.50, '2', '0'),
(206, 71, 53, '2026-03-04 09:15:43', 8, 0.0410, 8.80, '1', '1'),
(207, 71, 52, '2026-03-04 09:15:43', 6, 0.0310, 8.20, '1', '1'),
(208, 71, 75, '2026-03-04 09:15:43', 3, 0.0150, 6.30, '1', '0'),
(209, 72, 20, '2026-03-05 13:40:29', 9, 0.0470, 9.10, '1', '1'),
(210, 72, 32, '2026-03-05 13:40:29', 7, 0.0360, 8.40, '1', '1'),
(211, 72, 77, '2026-03-05 13:40:29', 4, 0.0200, 7.00, '2', '0'),
(212, 73, 3, '2026-03-06 15:30:58', 7, 0.0360, 8.30, '1', '1'),
(213, 73, 50, '2026-03-06 15:30:58', 5, 0.0250, 7.60, '1', '1'),
(214, 73, 7, '2026-03-06 15:30:58', 3, 0.0140, 6.20, '1', '0'),
(215, 74, 78, '2026-03-07 10:15:12', 10, 0.0530, 9.20, '1', '1'),
(216, 74, 1, '2026-03-07 10:15:12', 6, 0.0310, 8.10, '1', '1'),
(217, 74, 2, '2026-03-07 10:15:12', 4, 0.0190, 6.90, '2', '0'),
(218, 75, 81, '2026-03-08 14:25:36', 5, 0.0260, 7.50, '1', '1'),
(219, 75, 79, '2026-03-08 14:25:36', 4, 0.0210, 7.00, '1', '1'),
(220, 75, 75, '2026-03-08 14:25:36', 2, 0.0100, 5.60, '1', '0'),
(221, 76, 25, '2026-03-09 09:45:24', 8, 0.0410, 8.70, '1', '1'),
(222, 76, 26, '2026-03-09 09:45:24', 6, 0.0320, 8.10, '1', '1'),
(223, 76, 24, '2026-03-09 09:45:24', 4, 0.0200, 7.20, '1', '0'),
(224, 77, 18, '2026-03-10 11:30:48', 7, 0.0370, 8.40, '1', '1'),
(225, 77, 51, '2026-03-10 11:30:48', 5, 0.0260, 7.60, '1', '1'),
(226, 77, 50, '2026-03-10 11:30:48', 3, 0.0150, 6.30, '2', '0'),
(227, 78, 79, '2026-03-11 13:20:55', 9, 0.0470, 9.00, '1', '1'),
(228, 78, 74, '2026-03-11 13:20:55', 7, 0.0360, 8.30, '1', '1'),
(229, 78, 82, '2026-03-11 13:20:55', 4, 0.0200, 7.10, '1', '0'),
(230, 79, 71, '2026-03-12 15:40:11', 8, 0.0420, 8.60, '1', '1'),
(231, 79, 72, '2026-03-12 15:40:11', 6, 0.0310, 8.00, '1', '1'),
(232, 79, 70, '2026-03-12 15:40:11', 4, 0.0190, 6.80, '2', '0'),
(233, 80, 81, '2026-03-12 09:15:33', 6, 0.0320, 8.00, '1', '1'),
(234, 80, 79, '2026-03-12 09:15:33', 5, 0.0270, 7.60, '1', '1'),
(235, 80, 75, '2026-03-12 09:15:33', 3, 0.0150, 6.40, '1', '0'),
(236, 81, 77, '2026-03-13 10:30:47', 7, 0.0370, 8.30, '1', '1'),
(237, 81, 53, '2026-03-13 10:30:47', 5, 0.0250, 7.50, '1', '1'),
(238, 81, 78, '2026-03-13 10:30:47', 3, 0.0140, 6.20, '2', '0'),
(239, 82, 55, '2026-03-14 14:45:22', 4, 0.0220, 6.80, '1', '1'),
(240, 82, 56, '2026-03-14 14:45:22', 3, 0.0160, 6.10, '1', '1'),
(241, 82, 54, '2026-03-14 14:45:22', 2, 0.0090, 5.20, '1', '0'),
(242, 83, 74, '2026-03-15 11:20:18', 8, 0.0420, 8.70, '1', '1'),
(243, 83, 75, '2026-03-15 11:20:18', 6, 0.0300, 7.90, '1', '1'),
(244, 83, 79, '2026-03-15 11:20:18', 4, 0.0200, 7.00, '2', '0'),
(245, 84, 17, '2026-03-15 15:30:44', 7, 0.0370, 8.40, '1', '1'),
(246, 84, 78, '2026-03-15 15:30:44', 5, 0.0250, 7.50, '1', '1'),
(247, 84, 77, '2026-03-15 15:30:44', 2, 0.0110, 5.80, '1', '0'),
(248, 85, 33, '2026-03-16 09:20:36', 6, 0.0320, 8.10, '1', '1'),
(249, 85, 34, '2026-03-16 09:20:36', 4, 0.0220, 7.20, '1', '1'),
(250, 85, 35, '2026-03-16 09:20:36', 3, 0.0150, 6.30, '2', '0'),
(251, 86, 55, '2026-03-16 13:40:59', 5, 0.0270, 7.50, '1', '1'),
(252, 86, 56, '2026-03-16 13:40:59', 4, 0.0210, 7.00, '1', '1'),
(253, 86, 54, '2026-03-16 13:40:59', 2, 0.0100, 5.60, '1', '0'),
(254, 87, 73, '2026-03-17 10:15:23', 7, 0.0370, 8.30, '1', '1'),
(255, 87, 74, '2026-03-17 10:15:23', 5, 0.0260, 7.60, '1', '1'),
(256, 87, 82, '2026-03-17 10:15:23', 3, 0.0140, 6.20, '2', '0'),
(257, 88, 31, '2026-03-17 14:30:47', 4, 0.0220, 6.90, '1', '1'),
(258, 88, 32, '2026-03-17 14:30:47', 3, 0.0160, 6.30, '1', '1'),
(259, 88, 20, '2026-03-17 14:30:47', 2, 0.0090, 5.40, '1', '0'),
(260, 89, 28, '2026-03-18 09:45:11', 8, 0.0420, 8.70, '1', '1'),
(261, 89, 27, '2026-03-18 09:45:11', 6, 0.0310, 8.10, '1', '1'),
(262, 89, 5, '2026-03-18 09:45:11', 4, 0.0200, 7.10, '2', '0'),
(263, 90, 61, '2026-03-18 13:20:35', 9, 0.0470, 9.00, '1', '1'),
(264, 90, 62, '2026-03-18 13:20:35', 7, 0.0360, 8.40, '1', '1'),
(265, 90, 64, '2026-03-18 13:20:35', 4, 0.0190, 6.90, '1', '0'),
(266, 91, 50, '2026-03-19 10:30:42', 7, 0.0370, 8.30, '1', '1'),
(267, 91, 51, '2026-03-19 10:30:42', 6, 0.0310, 8.00, '1', '1'),
(268, 91, 52, '2026-03-19 10:30:42', 3, 0.0150, 6.40, '2', '0'),
(269, 92, 43, '2026-03-19 14:15:18', 5, 0.0260, 7.50, '1', '1'),
(270, 92, 44, '2026-03-19 14:15:18', 4, 0.0200, 7.00, '1', '1'),
(271, 92, 77, '2026-03-19 14:15:18', 2, 0.0100, 5.60, '1', '0'),
(272, 93, 76, '2026-03-20 09:45:29', 8, 0.0420, 8.70, '1', '1'),
(273, 93, 75, '2026-03-20 09:45:29', 6, 0.0300, 8.00, '1', '1'),
(274, 93, 77, '2026-03-20 09:45:29', 3, 0.0150, 6.30, '2', '0'),
(275, 94, 78, '2026-03-20 14:20:53', 7, 0.0360, 8.40, '1', '1'),
(276, 94, 28, '2026-03-20 14:20:53', 5, 0.0250, 7.60, '1', '1'),
(277, 94, 27, '2026-03-20 14:20:53', 3, 0.0140, 6.20, '1', '0'),
(278, 95, 79, '2026-03-21 10:30:14', 6, 0.0320, 8.10, '1', '1'),
(279, 95, 81, '2026-03-21 10:30:14', 5, 0.0260, 7.60, '1', '1'),
(280, 95, 82, '2026-03-21 10:30:14', 3, 0.0150, 6.40, '2', '0'),
(281, 96, 60, '2026-03-21 15:45:37', 5, 0.0260, 7.50, '1', '1'),
(282, 96, 50, '2026-03-21 15:45:37', 4, 0.0200, 7.00, '1', '1'),
(283, 96, 27, '2026-03-21 15:45:37', 2, 0.0090, 5.30, '1', '0'),
(284, 97, 1, '2026-03-22 09:15:48', 8, 0.0420, 8.70, '1', '1'),
(285, 97, 2, '2026-03-22 09:15:48', 6, 0.0310, 8.10, '1', '1'),
(286, 97, 70, '2026-03-22 09:15:48', 4, 0.0200, 7.10, '2', '0'),
(287, 98, 21, '2026-03-22 13:40:22', 7, 0.0370, 8.40, '1', '1'),
(288, 98, 22, '2026-03-22 13:40:22', 5, 0.0260, 7.60, '1', '1'),
(289, 98, 78, '2026-03-22 13:40:22', 3, 0.0140, 6.20, '1', '0'),
(290, 99, 77, '2026-03-23 10:20:55', 6, 0.0320, 8.10, '1', '1'),
(291, 99, 79, '2026-03-23 10:20:55', 4, 0.0210, 7.20, '1', '1'),
(292, 99, 81, '2026-03-23 10:20:55', 3, 0.0150, 6.40, '2', '0'),
(293, 100, 27, '2026-03-23 15:30:41', 9, 0.0470, 9.00, '1', '1'),
(294, 100, 28, '2026-03-23 15:30:41', 7, 0.0360, 8.40, '1', '1'),
(295, 100, 5, '2026-03-23 15:30:41', 5, 0.0250, 7.50, '1', '0'),
(296, 101, 73, '2026-03-24 09:30:17', 8, 0.0420, 8.70, '1', '1'),
(297, 101, 74, '2026-03-24 09:30:17', 6, 0.0310, 8.10, '1', '1'),
(298, 101, 82, '2026-03-24 09:30:17', 4, 0.0190, 6.90, '2', '0'),
(299, 102, 31, '2026-03-24 14:15:33', 7, 0.0370, 8.30, '1', '1'),
(300, 102, 32, '2026-03-24 14:15:33', 5, 0.0250, 7.50, '1', '1'),
(301, 102, 78, '2026-03-24 14:15:33', 3, 0.0140, 6.20, '1', '0'),
(302, 103, 39, '2026-03-25 10:45:28', 6, 0.0320, 8.00, '1', '1'),
(303, 103, 40, '2026-03-25 10:45:28', 4, 0.0200, 7.10, '1', '1'),
(304, 103, 41, '2026-03-25 10:45:28', 2, 0.0090, 5.40, '2', '0'),
(305, 104, 77, '2026-03-25 14:20:44', 7, 0.0370, 8.30, '1', '1'),
(306, 104, 79, '2026-03-25 14:20:44', 5, 0.0250, 7.60, '1', '1'),
(307, 104, 80, '2026-03-25 14:20:44', 3, 0.0150, 6.30, '1', '0'),
(308, 105, 15, '2026-03-26 09:15:52', 8, 0.0420, 8.60, '1', '1'),
(309, 105, 19, '2026-03-26 09:15:52', 6, 0.0310, 8.00, '1', '1'),
(310, 105, 78, '2026-03-26 09:15:52', 4, 0.0200, 7.00, '2', '0'),
(311, 106, 74, '2026-03-26 15:30:36', 9, 0.0470, 9.10, '1', '1'),
(312, 106, 82, '2026-03-26 15:30:36', 7, 0.0360, 8.40, '1', '1'),
(313, 106, 79, '2026-03-26 15:30:36', 4, 0.0190, 6.90, '1', '0'),
(314, 107, 16, '2026-03-27 10:20:19', 6, 0.0320, 8.10, '1', '1'),
(315, 107, 78, '2026-03-27 10:20:19', 4, 0.0210, 7.20, '1', '1'),
(316, 107, 77, '2026-03-27 10:20:19', 2, 0.0100, 5.60, '2', '0'),
(317, 108, 1, '2026-03-27 13:45:27', 5, 0.0260, 7.60, '1', '1'),
(318, 108, 2, '2026-03-27 13:45:27', 4, 0.0200, 7.10, '1', '1'),
(319, 108, 33, '2026-03-27 13:45:27', 3, 0.0150, 6.30, '1', '0'),
(320, 109, 9, '2026-03-28 09:30:48', 7, 0.0370, 8.40, '1', '1'),
(321, 109, 10, '2026-03-28 09:30:48', 5, 0.0250, 7.60, '1', '1'),
(322, 109, 75, '2026-03-28 09:30:48', 3, 0.0140, 6.20, '2', '0'),
(323, 110, 81, '2026-03-28 14:15:12', 8, 0.0420, 8.70, '1', '1'),
(324, 110, 82, '2026-03-28 14:15:12', 6, 0.0310, 8.10, '1', '1'),
(325, 110, 79, '2026-03-28 14:15:12', 4, 0.0200, 7.10, '1', '0'),
(326, 111, 26, '2026-03-29 10:45:33', 7, 0.0360, 8.30, '1', '1'),
(327, 111, 78, '2026-03-29 10:45:33', 5, 0.0250, 7.60, '1', '1'),
(328, 111, 77, '2026-03-29 10:45:33', 3, 0.0150, 6.30, '2', '0'),
(329, 112, 41, '2026-03-29 13:30:55', 9, 0.0470, 9.00, '1', '1'),
(330, 112, 42, '2026-03-29 13:30:55', 7, 0.0360, 8.40, '1', '1'),
(331, 112, 75, '2026-03-29 13:30:55', 4, 0.0190, 6.90, '1', '0'),
(332, 113, 70, '2026-03-30 09:20:41', 8, 0.0420, 8.60, '1', '1'),
(333, 113, 71, '2026-03-30 09:20:41', 6, 0.0310, 8.00, '1', '1'),
(334, 113, 74, '2026-03-30 09:20:41', 3, 0.0150, 6.40, '2', '0'),
(335, 114, 2, '2026-03-30 14:45:27', 7, 0.0370, 8.30, '1', '1'),
(336, 114, 55, '2026-03-30 14:45:27', 5, 0.0250, 7.60, '1', '1');

-- 继续生成article_tag_relation表测试数据，id从338开始
INSERT INTO `article_tag_relation` (`id`, `article_id`, `tag_id`, `created_at`, `hit_count`, `density`, `score`, `source`, `confirmed`) VALUES
(338, 115, 50, '2026-03-31 10:15:33', 8, 0.0420, 8.70, '1', '1'),
(339, 115, 51, '2026-03-31 10:15:33', 6, 0.0310, 8.10, '1', '1'),
(340, 115, 52, '2026-03-31 10:15:33', 4, 0.0200, 7.10, '2', '0'),
(341, 116, 79, '2026-03-31 15:30:44', 5, 0.0260, 7.50, '1', '1'),
(342, 116, 80, '2026-03-31 15:30:44', 4, 0.0210, 7.00, '1', '1'),
(343, 116, 82, '2026-03-31 15:30:44', 2, 0.0100, 5.60, '1', '0'),
(344, 117, 22, '2026-04-01 09:45:28', 7, 0.0370, 8.40, '1', '1'),
(345, 117, 78, '2026-04-01 09:45:28', 5, 0.0250, 7.60, '1', '1'),
(346, 117, 77, '2026-04-01 09:45:28', 3, 0.0140, 6.20, '2', '0'),
(347, 118, 74, '2026-04-01 14:20:16', 8, 0.0420, 8.60, '1', '1'),
(348, 118, 75, '2026-04-01 14:20:16', 6, 0.0310, 8.00, '1', '1'),
(349, 118, 79, '2026-04-01 14:20:16', 4, 0.0200, 7.00, '1', '0'),
(350, 119, 43, '2026-04-02 10:30:52', 6, 0.0320, 8.10, '1', '1'),
(351, 119, 44, '2026-04-02 10:30:52', 4, 0.0210, 7.20, '1', '1'),
(352, 119, 77, '2026-04-02 10:30:52', 3, 0.0150, 6.30, '2', '0'),
(353, 120, 7, '2026-04-02 13:45:37', 7, 0.0370, 8.30, '1', '1'),
(354, 120, 50, '2026-04-02 13:45:37', 5, 0.0250, 7.60, '1', '1'),
(355, 120, 51, '2026-04-02 13:45:37', 3, 0.0140, 6.20, '1', '0'),
(356, 121, 74, '2026-04-03 09:30:22', 9, 0.0470, 9.00, '1', '1'),
(357, 121, 79, '2026-04-03 09:30:22', 7, 0.0360, 8.40, '1', '1'),
(358, 121, 82, '2026-04-03 09:30:22', 4, 0.0190, 6.90, '2', '0'),
(359, 122, 55, '2026-04-03 14:15:48', 6, 0.0320, 8.00, '1', '1'),
(360, 122, 56, '2026-04-03 14:15:48', 5, 0.0260, 7.60, '1', '1'),
(361, 122, 78, '2026-04-03 14:15:48', 3, 0.0150, 6.40, '1', '0'),
(362, 123, 31, '2026-04-04 10:20:33', 7, 0.0370, 8.30, '1', '1'),
(363, 123, 32, '2026-04-04 10:20:33', 5, 0.0250, 7.50, '1', '1'),
(364, 123, 54, '2026-04-04 10:20:33', 3, 0.0140, 6.20, '2', '0'),
(365, 124, 77, '2026-04-04 15:30:19', 6, 0.0320, 8.10, '1', '1'),
(366, 124, 78, '2026-04-04 15:30:19', 4, 0.0210, 7.20, '1', '1'),
(367, 124, 70, '2026-04-04 15:30:19', 2, 0.0090, 5.40, '1', '0'),
(368, 125, 74, '2026-04-05 09:45:44', 8, 0.0420, 8.70, '1', '1'),
(369, 125, 79, '2026-04-05 09:45:44', 6, 0.0310, 8.10, '1', '1'),
(370, 125, 82, '2026-04-05 09:45:44', 4, 0.0200, 7.10, '2', '0'),
(371, 126, 27, '2026-04-05 13:20:57', 7, 0.0370, 8.40, '1', '1'),
(372, 126, 28, '2026-04-05 13:20:57', 5, 0.0250, 7.60, '1', '1'),
(373, 126, 50, '2026-04-05 13:20:57', 3, 0.0140, 6.20, '1', '0'),
(374, 127, 33, '2026-04-06 10:15:31', 6, 0.0320, 8.00, '1', '1'),
(375, 127, 34, '2026-04-06 10:15:31', 4, 0.0210, 7.10, '1', '1'),
(376, 127, 35, '2026-04-06 10:15:31', 3, 0.0150, 6.30, '2', '0'),
(377, 128, 28, '2026-04-06 14:30:48', 8, 0.0420, 8.60, '1', '1'),
(378, 128, 29, '2026-04-06 14:30:48', 6, 0.0310, 8.00, '1', '1'),
(379, 128, 77, '2026-04-06 14:30:48', 4, 0.0200, 7.00, '1', '0'),
(380, 129, 79, '2026-04-07 09:45:22', 7, 0.0370, 8.30, '1', '1'),
(381, 129, 80, '2026-04-07 09:45:22', 5, 0.0250, 7.60, '1', '1'),
(382, 129, 82, '2026-04-07 09:45:22', 3, 0.0140, 6.20, '2', '0'),
(383, 130, 50, '2026-04-07 15:20:36', 9, 0.0470, 9.10, '1', '1'),
(384, 130, 51, '2026-04-07 15:20:36', 7, 0.0360, 8.40, '1', '1'),
(385, 130, 75, '2026-04-07 15:20:36', 5, 0.0250, 7.50, '1', '0'),
(386, 131, 1, '2026-04-08 10:30:44', 6, 0.0320, 8.10, '1', '1'),
(387, 131, 2, '2026-04-08 10:30:44', 4, 0.0210, 7.20, '1', '1'),
(388, 131, 10, '2026-04-08 10:30:44', 3, 0.0150, 6.30, '2', '0'),
(389, 132, 51, '2026-04-08 13:45:29', 7, 0.0370, 8.40, '1', '1'),
(390, 132, 52, '2026-04-08 13:45:29', 5, 0.0250, 7.60, '1', '1'),
(391, 132, 78, '2026-04-08 13:45:29', 3, 0.0140, 6.20, '1', '0'),
(392, 133, 73, '2026-04-09 09:30:55', 8, 0.0420, 8.70, '1', '1'),
(393, 133, 74, '2026-04-09 09:30:55', 6, 0.0310, 8.10, '1', '1'),
(394, 133, 82, '2026-04-09 09:30:55', 4, 0.0200, 7.10, '2', '0'),
(395, 134, 20, '2026-04-09 14:15:41', 7, 0.0370, 8.30, '1', '1'),
(396, 134, 55, '2026-04-09 14:15:41', 5, 0.0250, 7.60, '1', '1'),
(397, 134, 56, '2026-04-09 14:15:41', 3, 0.0140, 6.20, '1', '0'),
(398, 135, 6, '2026-04-10 10:40:18', 8, 0.0420, 8.60, '1', '1'),
(399, 135, 19, '2026-04-10 10:40:18', 6, 0.0310, 8.00, '1', '1'),
(400, 135, 78, '2026-04-10 10:40:18', 4, 0.0200, 7.00, '2', '0'),
(401, 136, 1, '2026-04-10 13:30:33', 5, 0.0260, 7.50, '1', '1'),
(402, 136, 2, '2026-04-10 13:30:33', 4, 0.0210, 7.00, '1', '1'),
(403, 136, 20, '2026-04-10 13:30:33', 2, 0.0100, 5.60, '1', '0'),
(404, 137, 74, '2026-04-11 09:45:27', 9, 0.0470, 9.00, '1', '1'),
(405, 137, 79, '2026-04-11 09:45:27', 7, 0.0360, 8.40, '1', '1'),
(406, 137, 82, '2026-04-11 09:45:27', 4, 0.0190, 6.90, '2', '0'),
(407, 138, 68, '2026-04-11 14:30:52', 6, 0.0320, 8.00, '1', '1'),
(408, 138, 69, '2026-04-11 14:30:52', 4, 0.0210, 7.10, '1', '1'),
(409, 138, 75, '2026-04-11 14:30:52', 3, 0.0150, 6.30, '1', '0'),
(410, 139, 50, '2026-04-12 10:15:44', 7, 0.0370, 8.30, '1', '1'),
(411, 139, 51, '2026-04-12 10:15:44', 5, 0.0250, 7.60, '1', '1'),
(412, 139, 77, '2026-04-12 10:15:44', 3, 0.0140, 6.20, '2', '0'),
(413, 140, 79, '2026-04-12 15:20:19', 8, 0.0420, 8.70, '1', '1'),
(414, 140, 81, '2026-04-12 15:20:19', 6, 0.0310, 8.10, '1', '1'),
(415, 140, 82, '2026-04-12 15:20:19', 4, 0.0200, 7.10, '1', '0'),
(416, 141, 4, '2026-04-13 09:30:36', 7, 0.0370, 8.40, '1', '1'),
(417, 141, 5, '2026-04-13 09:30:36', 5, 0.0250, 7.60, '1', '1'),
(418, 141, 6, '2026-04-13 09:30:36', 3, 0.0140, 6.20, '2', '0'),
(419, 142, 61, '2026-04-13 14:45:28', 8, 0.0420, 8.60, '1', '1'),
(420, 142, 62, '2026-04-13 14:45:28', 6, 0.0310, 8.00, '1', '1'),
(421, 142, 63, '2026-04-13 14:45:28', 4, 0.0200, 7.00, '1', '0'),
(422, 143, 15, '2026-04-14 10:20:41', 7, 0.0370, 8.30, '1', '1'),
(423, 143, 16, '2026-04-14 10:20:41', 5, 0.0250, 7.60, '1', '1'),
(424, 143, 17, '2026-04-14 10:20:41', 3, 0.0140, 6.20, '2', '0'),
(425, 144, 18, '2026-04-14 15:35:52', 8, 0.0420, 8.70, '1', '1'),
(426, 144, 19, '2026-04-14 15:35:52', 6, 0.0310, 8.10, '1', '1'),
(427, 144, 20, '2026-04-14 15:35:52', 4, 0.0200, 7.10, '1', '0'),
(428, 145, 21, '2026-04-15 09:45:33', 7, 0.0370, 8.40, '1', '1'),
(429, 145, 22, '2026-04-15 09:45:33', 5, 0.0250, 7.60, '1', '1'),
(430, 145, 23, '2026-04-15 09:45:33', 3, 0.0140, 6.20, '2', '0'),
(431, 146, 24, '2026-04-15 14:20:47', 6, 0.0320, 8.10, '1', '1'),
(432, 146, 25, '2026-04-15 14:20:47', 4, 0.0210, 7.20, '1', '1'),
(433, 146, 26, '2026-04-15 14:20:47', 3, 0.0150, 6.30, '1', '0'),
(434, 147, 27, '2026-04-16 10:30:22', 8, 0.0420, 8.60, '1', '1'),
(435, 147, 28, '2026-04-16 10:30:22', 6, 0.0310, 8.00, '1', '1'),
(436, 147, 29, '2026-04-16 10:30:22', 4, 0.0200, 7.00, '2', '0'),
(437, 148, 30, '2026-04-16 15:45:38', 7, 0.0370, 8.30, '1', '1'),
(438, 148, 31, '2026-04-16 15:45:38', 5, 0.0250, 7.60, '1', '1'),
(439, 148, 32, '2026-04-16 15:45:38', 3, 0.0140, 6.20, '1', '0'),
(440, 149, 33, '2026-04-17 09:20:44', 6, 0.0320, 8.00, '1', '1'),
(441, 149, 34, '2026-04-17 09:20:44', 4, 0.0210, 7.10, '1', '1'),
(442, 149, 35, '2026-04-17 09:20:44', 3, 0.0150, 6.30, '2', '0'),
(443, 150, 36, '2026-04-17 14:30:19', 7, 0.0370, 8.40, '1', '1'),
(444, 150, 37, '2026-04-17 14:30:19', 5, 0.0250, 7.60, '1', '1'),
(445, 150, 38, '2026-04-17 14:30:19', 3, 0.0140, 6.20, '1', '0'),
(446, 151, 39, '2026-04-18 10:15:28', 8, 0.0420, 8.70, '1', '1'),
(447, 151, 40, '2026-04-18 10:15:28', 6, 0.0310, 8.10, '1', '1'),
(448, 151, 41, '2026-04-18 10:15:28', 4, 0.0200, 7.10, '2', '0'),
(449, 152, 42, '2026-04-18 15:40:33', 7, 0.0370, 8.30, '1', '1'),
(450, 152, 43, '2026-04-18 15:40:33', 5, 0.0250, 7.60, '1', '1'),
(451, 152, 44, '2026-04-18 15:40:33', 3, 0.0140, 6.20, '1', '0'),
(452, 153, 45, '2026-04-19 09:30:47', 6, 0.0320, 8.10, '1', '1'),
(453, 153, 46, '2026-04-19 09:30:47', 4, 0.0210, 7.20, '1', '1'),
(454, 153, 47, '2026-04-19 09:30:47', 3, 0.0150, 6.30, '2', '0'),
(455, 154, 48, '2026-04-19 14:20:52', 7, 0.0370, 8.40, '1', '1'),
(456, 154, 49, '2026-04-19 14:20:52', 5, 0.0250, 7.60, '1', '1'),
(457, 154, 50, '2026-04-19 14:20:52', 3, 0.0140, 6.20, '1', '0'),
(458, 155, 51, '2026-04-20 10:45:29', 8, 0.0420, 8.60, '1', '1'),
(459, 155, 52, '2026-04-20 10:45:29', 6, 0.0310, 8.00, '1', '1'),
(460, 155, 53, '2026-04-20 10:45:29', 4, 0.0200, 7.00, '2', '0'),
(461, 156, 54, '2026-04-20 15:30:36', 7, 0.0370, 8.30, '1', '1'),
(462, 156, 55, '2026-04-20 15:30:36', 5, 0.0250, 7.60, '1', '1'),
(463, 156, 56, '2026-04-20 15:30:36', 3, 0.0140, 6.20, '1', '0'),
(464, 157, 57, '2026-04-21 09:15:41', 6, 0.0320, 8.00, '1', '1'),
(465, 157, 58, '2026-04-21 09:15:41', 4, 0.0210, 7.10, '1', '1'),
(466, 157, 59, '2026-04-21 09:15:41', 3, 0.0150, 6.30, '2', '0'),
(467, 158, 60, '2026-04-21 14:45:28', 7, 0.0370, 8.40, '1', '1'),
(468, 158, 61, '2026-04-21 14:45:28', 5, 0.0250, 7.60, '1', '1'),
(469, 158, 62, '2026-04-21 14:45:28', 3, 0.0140, 6.20, '1', '0'),
(470, 159, 63, '2026-04-22 10:30:33', 8, 0.0420, 8.70, '1', '1');


INSERT INTO search_hot (search_term, search_count, created_at, updated_at) VALUES
-- 编程语言与技术栈
('Python入门教程', 1560, '2024-03-15 09:30:00', '2024-03-15 14:20:00'),
('Java面试宝典', 1420, '2024-03-14 11:15:00', '2024-03-15 16:45:00'),
('JavaScript闭包原理', 1280, '2024-03-13 10:45:00', '2024-03-15 13:30:00'),
('Golang实战项目', 1150, '2024-03-12 14:20:00', '2024-03-15 15:10:00'),
('C++性能优化', 980, '2024-03-11 08:50:00', '2024-03-15 12:25:00'),
('Rust学习路线', 920, '2024-03-10 16:30:00', '2024-03-15 11:40:00'),
('PHP最新框架', 870, '2024-03-09 13:15:00', '2024-03-15 10:55:00'),
('TypeScript配置问题', 810, '2024-03-08 15:40:00', '2024-03-15 09:20:00'),
('Kotlin安卓开发', 760, '2024-03-07 09:25:00', '2024-03-15 17:30:00'),
('Swift UI教程', 720, '2024-03-06 11:50:00', '2024-03-15 14:45:00'),

-- 开发工具与环境
('VS Code插件推荐', 1350, '2024-03-15 08:15:00', '2024-03-15 18:20:00'),
('Git命令大全', 1240, '2024-03-14 10:30:00', '2024-03-15 16:15:00'),
('Docker部署实战', 1120, '2024-03-13 13:45:00', '2024-03-15 14:30:00'),
('Linux常用命令', 1050, '2024-03-12 09:10:00', '2024-03-15 13:20:00'),
('Nginx配置技巧', 960, '2024-03-11 14:25:00', '2024-03-15 15:45:00'),
('IDEA快捷键', 890, '2024-03-10 16:50:00', '2024-03-15 12:10:00'),
('Postman测试接口', 830, '2024-03-09 11:35:00', '2024-03-15 11:25:00'),
('Jenkins持续集成', 780, '2024-03-08 08:45:00', '2024-03-15 17:15:00'),
('Kubernetes入门', 740, '2024-03-07 15:20:00', '2024-03-15 10:30:00'),
('Shell脚本编写', 690, '2024-03-06 12:55:00', '2024-03-15 09:45:00'),

-- 前端开发
('Vue3响应式原理', 1480, '2024-03-15 10:20:00', '2024-03-15 19:30:00'),
('React Hooks使用', 1360, '2024-03-14 13:40:00', '2024-03-15 18:15:00'),
('CSS Flex布局', 1210, '2024-03-13 08:55:00', '2024-03-15 16:40:00'),
('Webpack打包优化', 1090, '2024-03-12 16:10:00', '2024-03-15 14:50:00'),
('前端性能优化方案', 1010, '2024-03-11 10:35:00', '2024-03-15 13:35:00'),
('小程序开发教程', 950, '2024-03-10 14:50:00', '2024-03-15 15:25:00'),
('TypeScript泛型', 880, '2024-03-09 09:15:00', '2024-03-15 12:45:00'),
('Node.js后端搭建', 820, '2024-03-08 11:30:00', '2024-03-15 11:50:00'),
('Electron桌面应用', 770, '2024-03-07 15:55:00', '2024-03-15 10:15:00'),
('移动端适配方案', 730, '2024-03-06 08:20:00', '2024-03-15 17:40:00'),

-- 后端开发
('Spring Boot配置', 1420, '2024-03-15 14:10:00', '2024-03-15 20:25:00'),
('数据库索引优化', 1300, '2024-03-14 09:25:00', '2024-03-15 19:15:00'),
('Redis缓存设计', 1180, '2024-03-13 12:40:00', '2024-03-15 18:30:00'),
('微服务架构设计', 1100, '2024-03-12 15:55:00', '2024-03-15 17:20:00'),
('消息队列选型', 1030, '2024-03-11 08:20:00', '2024-03-15 16:50:00'),
('API设计规范', 970, '2024-03-10 10:45:00', '2024-03-15 15:35:00'),
('分布式事务解决方案', 910, '2024-03-09 13:10:00', '2024-03-15 14:40:00'),
('MySQL调优经验', 860, '2024-03-08 16:35:00', '2024-03-15 13:10:00'),
('MongoDB使用场景', 800, '2024-03-07 09:50:00', '2024-03-15 12:30:00'),
('Elasticsearch搜索', 750, '2024-03-06 12:15:00', '2024-03-15 11:20:00'),

-- 算法与数据结构
('动态规划解题思路', 1250, '2024-03-15 11:40:00', '2024-03-15 21:10:00'),
('LeetCode刷题路线', 1160, '2024-03-14 14:55:00', '2024-03-15 20:40:00'),
('排序算法比较', 1080, '2024-03-13 08:10:00', '2024-03-15 19:50:00'),
('二叉树遍历方法', 990, '2024-03-12 11:25:00', '2024-03-15 18:45:00'),
('哈希表应用场景', 930, '2024-03-11 14:50:00', '2024-03-15 17:55:00'),
('图论基础算法', 870, '2024-03-10 09:15:00', '2024-03-15 16:25:00'),
('回溯算法模板', 820, '2024-03-09 12:40:00', '2024-03-15 15:15:00'),
('位运算技巧', 780, '2024-03-08 15:05:00', '2024-03-15 14:05:00'),
('双指针法总结', 730, '2024-03-07 10:30:00', '2024-03-15 13:50:00'),
('贪心算法实例', 690, '2024-03-06 13:55:00', '2024-03-15 12:20:00'),

-- 职场与学习
('程序员简历怎么写', 1380, '2024-03-15 13:20:00', '2024-03-15 22:05:00'),
('技术面试准备', 1260, '2024-03-14 16:45:00', '2024-03-15 21:30:00'),
('程序员副业推荐', 1140, '2024-03-13 10:00:00', '2024-03-15 20:15:00'),
('职业发展规划', 1060, '2024-03-12 13:25:00', '2024-03-15 19:25:00'),
('远程工作机会', 980, '2024-03-11 08:50:00', '2024-03-15 18:35:00'),
('技术书籍推荐', 920, '2024-03-10 12:15:00', '2024-03-15 17:45:00'),
('编程学习路线', 860, '2024-03-09 15:40:00', '2024-03-15 16:55:00'),
('开源项目贡献指南', 810, '2024-03-08 09:05:00', '2024-03-15 15:50:00'),
('技术博客写作', 760, '2024-03-07 12:30:00', '2024-03-15 14:25:00'),
('技术会议分享', 720, '2024-03-06 15:55:00', '2024-03-15 13:40:00'),

-- 计算机基础
('计算机网络面试题', 1220, '2024-03-15 09:45:00', '2024-03-15 23:10:00'),
('操作系统原理', 1130, '2024-03-14 13:10:00', '2024-03-15 22:40:00'),
('编译原理入门', 1050, '2024-03-13 16:35:00', '2024-03-15 21:55:00'),
('计算机组成原理', 970, '2024-03-12 10:00:00', '2024-03-15 20:50:00'),
('HTTP协议详解', 910, '2024-03-11 13:25:00', '2024-03-15 19:40:00'),
('TCP/IP三次握手', 850, '2024-03-10 08:50:00', '2024-03-15 18:20:00'),
('进程与线程区别', 800, '2024-03-09 12:15:00', '2024-03-15 17:10:00'),
('内存管理机制', 750, '2024-03-08 15:40:00', '2024-03-15 16:30:00'),
('文件系统原理', 710, '2024-03-07 09:05:00', '2024-03-15 15:40:00'),
('加密算法基础', 670, '2024-03-06 12:30:00', '2024-03-15 14:55:00'),

-- 新技术与趋势
('AI编程工具', 1320, '2024-03-15 15:30:00', '2024-03-16 00:25:00'),
('低代码平台', 1240, '2024-03-14 08:55:00', '2024-03-15 23:35:00'),
('Web3.0开发', 1150, '2024-03-13 12:20:00', '2024-03-15 22:50:00'),
('区块链入门', 1070, '2024-03-12 15:45:00', '2024-03-15 22:15:00'),
('物联网技术', 990, '2024-03-11 10:10:00', '2024-03-15 21:20:00'),
('边缘计算应用', 930, '2024-03-10 13:35:00', '2024-03-15 20:45:00'),
('量子计算简介', 880, '2024-03-09 08:00:00', '2024-03-15 19:55:00'),
('元宇宙开发', 830, '2024-03-08 11:25:00', '2024-03-15 18:50:00'),
('RPA自动化', 780, '2024-03-07 14:50:00', '2024-03-15 17:35:00'),
('AR/VR开发', 740, '2024-03-06 09:15:00', '2024-03-15 16:45:00'),

-- 实战与问题
('项目部署常见问题', 1180, '2024-03-15 16:55:00', '2024-03-16 01:15:00'),
('代码重构技巧', 1100, '2024-03-14 10:20:00', '2024-03-16 00:40:00'),
('BUG调试方法', 1030, '2024-03-13 13:45:00', '2024-03-15 23:55:00'),
('代码规范检查', 960, '2024-03-12 17:10:00', '2024-03-15 23:20:00'),
('单元测试编写', 900, '2024-03-11 09:35:00', '2024-03-15 22:30:00'),
('压力测试工具', 850, '2024-03-10 12:00:00', '2024-03-15 21:45:00'),
('安全漏洞防范', 800, '2024-03-09 15:25:00', '2024-03-15 20:35:00'),
('日志分析技巧', 750, '2024-03-08 08:50:00', '2024-03-15 19:25:00'),
('数据库备份方案', 710, '2024-03-07 12:15:00', '2024-03-15 18:15:00'),
('API接口安全', 670, '2024-03-06 15:40:00', '2024-03-15 17:05:00'),

-- 架构与设计
('系统设计面试', 1290, '2024-03-15 08:10:00', '2024-03-16 02:05:00'),
('架构师成长路径', 1200, '2024-03-14 11:35:00', '2024-03-16 01:30:00'),
('设计模式实践', 1120, '2024-03-13 14:50:00', '2024-03-16 00:50:00'),
('高并发解决方案', 1040, '2024-03-12 09:15:00', '2024-03-15 23:45:00'),
('CAP理论理解', 970, '2024-03-11 12:40:00', '2024-03-15 22:55:00'),
('DDD领域驱动', 910, '2024-03-10 16:05:00', '2024-03-15 22:10:00'),
('微服务拆分原则', 860, '2024-03-09 10:30:00', '2024-03-15 21:25:00'),
('代码分层架构', 810, '2024-03-08 13:55:00', '2024-03-15 20:15:00'),
('前后端分离方案', 760, '2024-03-07 17:20:00', '2024-03-15 19:05:00'),
('负载均衡策略', 720, '2024-03-06 08:45:00', '2024-03-15 18:00:00');

INSERT INTO `article_tag` (`name`, `slug`, `description`, `color`) VALUES
-- 框架类
('Spring Cloud', 'spring-cloud', 'Spring Cloud微服务框架', '#6db33f'),
('MyBatis', 'mybatis', 'Java持久层框架', '#e95420'),
('Vite', 'vite', '下一代前端构建工具', '#646cff'),
('Webpack', 'webpack', '前端模块打包工具', '#8dd6f9'),
('Express.js', 'express', 'Node.js Web框架', '#000000'),
('NestJS', 'nestjs', 'Node.js渐进式框架', '#ea2845'),
('Next.js', 'nextjs', 'React全栈框架', '#000000'),
('Nuxt.js', 'nuxt', 'Vue.js通用应用框架', '#00c58e'),

-- 数据库类
('PostgreSQL', 'postgresql', '开源关系型数据库', '#336791'),
('MongoDB', 'mongodb', '文档型NoSQL数据库', '#47a248'),
('Elasticsearch', 'elasticsearch', '搜索和分析引擎', '#005571'),
('Kafka', 'kafka', '分布式消息队列', '#231f20'),
('MySQL优化', 'mysql-optimization', 'MySQL性能优化', '#4479a1'),

-- 语言类
('TypeScript', 'typescript', 'JavaScript超集语言', '#3178c6'),
('Go语言', 'golang', 'Google开发的编程语言', '#00add8'),
('Rust', 'rust', '系统编程语言', '#000000'),
('Kotlin', 'kotlin', 'Android官方开发语言', '#7f52ff'),
('Shell脚本', 'shell', 'Shell脚本编程', '#4eaa25'),

-- 运维部署类
('Linux', 'linux', 'Linux操作系统', '#fcc624'),
('Nginx', 'nginx', '高性能Web服务器', '#269539'),
('Kubernetes', 'kubernetes', '容器编排平台', '#326ce5'),
('Jenkins', 'jenkins', '持续集成工具', '#d24939'),
('Git', 'git', '分布式版本控制系统', '#f05032'),
('Docker Compose', 'docker-compose', 'Docker容器编排', '#2496ed'),

-- 前端技术
('Node.js', 'nodejs', 'JavaScript运行时环境', '#339933'),
('ES6+', 'es6-plus', 'ECMAScript新特性', '#f7df1e'),
('CSS3', 'css3', '层叠样式表3', '#1572b6'),
('Sass/SCSS', 'sass', 'CSS预处理器', '#cc6699'),
('Less', 'less', 'CSS预处理器', '#1d365d'),
('Bootstrap', 'bootstrap', '前端框架', '#7952b3'),
('Element UI', 'element-ui', 'Vue组件库', '#409eff'),
('Ant Design', 'ant-design', 'React组件库', '#0170fe'),

-- 移动开发
('Flutter', 'flutter', 'Google移动UI框架', '#02569b'),
('React Native', 'react-native', '跨平台移动应用框架', '#61dafb'),
('微信小程序', 'wechat-miniprogram', '微信小程序开发', '#07c160'),
('uni-app', 'uni-app', '跨平台应用框架', '#f1de15'),


-- 工具类
('VS Code', 'vscode', '微软代码编辑器', '#007acc'),
('IntelliJ IDEA', 'intellij-idea', 'Java集成开发环境', '#000000'),
('PyCharm', 'pycharm', 'Python集成开发环境', '#21d789'),
('Postman', 'postman', 'API调试工具', '#ff6c37'),
('Swagger', 'swagger', 'API文档工具', '#85ea2d'),
('Jest', 'jest', 'JavaScript测试框架', '#c21325'),
('Mocha', 'mocha', 'JavaScript测试框架', '#8d6748'),

-- 架构与设计
('微服务', 'microservices', '微服务架构', '#ff6b6b'),
('分布式系统', 'distributed-system', '分布式系统设计', '#4ecdc4'),
('DDD', 'ddd', '领域驱动设计', '#ffd166'),
('设计模式', 'design-patterns', '软件设计模式', '#06d6a0'),
('RESTful API', 'restful-api', 'RESTful API设计', '#118ab2'),
('GraphQL', 'graphql', '数据查询语言', '#e10098'),
('gRPC', 'grpc', '高性能RPC框架', '#5a6ed8'),

-- 云计算
('AWS', 'aws', '亚马逊云服务', '#ff9900'),
('阿里云', 'alibaba-cloud', '阿里云服务', '#ff6a00'),
('腾讯云', 'tencent-cloud', '腾讯云服务', '#3cc195'),
('Serverless', 'serverless', '无服务器架构', '#fd5750'),

-- 人工智能
('机器学习', 'machine-learning', '机器学习算法', '#007c97'),
('深度学习', 'deep-learning', '深度学习技术', '#e63946'),
('TensorFlow', 'tensorflow', '深度学习框架', '#ff6f00'),
('PyTorch', 'pytorch', '深度学习框架', '#ee4c2c'),
('OpenAI', 'openai', '人工智能研究机构', '#412991'),

-- 其他热门技术
('区块链', 'blockchain', '区块链技术', '#121d33'),
('Web3', 'web3', '下一代互联网技术', '#f16822'),
('物联网', 'iot', '物联网技术', '#5d9cec'),
('边缘计算', 'edge-computing', '边缘计算技术', '#48cfad'),

-- 职业发展
('面试题', 'interview', '面试相关题目', '#8e44ad'),
('算法题', 'algorithm', '算法题目与解析', '#e74c3c'),
('LeetCode', 'leetcode', '算法题库', '#ffa116'),
('简历', 'resume', '简历编写技巧', '#27ae60'),
('职业规划', 'career-planning', '职业发展规划', '#3498db'),

-- 实战项目
('实战项目', 'project', '实战项目经验', '#9b59b6'),
('开源项目', 'open-source', '开源项目贡献', '#2ecc71'),
('代码规范', 'code-style', '代码规范与风格', '#f39c12'),
('性能优化', 'performance', '性能优化技巧', '#d35400'),

-- 社区文章类型
('技术分享', 'tech-sharing', '技术经验分享', '#1abc9c'),
('问题解答', 'qna', '问题与解答', '#e67e22'),
('资源推荐', 'resource', '学习资源推荐', '#34495e'),
('个人成长', 'growth', '个人成长经历', '#16a085');

-- 为文章分配多个标签
-- 注意：这里为每篇文章分配2-4个标签，确保多样性
INSERT INTO `article_tag_relation` (`article_id`, `tag_id`) VALUES
-- 文章1: 三年后端开发，我总结的MySQL调优实战经验
(1, 14), -- MySQL优化
(1, 48), -- 性能优化
(1, 31), -- 数据库
(1, 37), -- 实战项目

-- 文章2: Vue3项目从0到上线部署全流程记录
(2, 3),  -- Vue.js
(2, 19), -- Node.js
(2, 30), -- Webpack
(2, 41), -- 实战项目

-- 文章3: 一次线上内存泄漏排查全过程
(3, 14), -- Java
(3, 20), -- JVM
(3, 48), -- 性能优化
(3, 49), -- 问题解答

-- 文章4: Spring Boot整合Redis实现分布式锁
(4, 4),  -- Spring Boot
(4, 6),  -- Redis
(4, 32), -- 分布式系统
(4, 37), -- 实战项目

-- 文章5: 前端性能优化：从8秒到1.5秒的蜕变
(5, 30), -- Webpack
(5, 48), -- 性能优化
(5, 25), -- 前端
(5, 41), -- 实战项目

-- 文章6: 微服务架构下的日志收集方案
(6, 4),  -- Spring Boot
(6, 32), -- 分布式系统
(6, 33), -- 微服务
(6, 42), -- ELK

-- 文章7: Git团队协作规范与最佳实践
(7, 23), -- Git
(7, 43), -- 团队协作
(7, 44), -- 代码规范
(7, 50), -- 技术分享

-- 文章8: Docker从入门到生产环境部署
(8, 5),  -- Docker
(8, 24), -- Docker Compose
(8, 37), -- 实战项目
(8, 50), -- 技术分享

-- 文章9: React Hooks使用中的常见陷阱
(9, 2),  -- React
(9, 45), -- Hooks
(9, 49), -- 问题解答
(9, 50), -- 技术分享

-- 文章10: 从零搭建一个企业级Node.js后端服务
(10, 19), -- Node.js
(10, 34), -- Express.js
(10, 37), -- 实战项目
(10, 50), -- 技术分享

-- 文章11: SQL优化：如何让慢查询快起来
(11, 14), -- MySQL优化
(11, 31), -- 数据库
(11, 48), -- 性能优化
(11, 50), -- 技术分享

-- 文章12: 微信小程序开发避坑指南
(12, 28), -- 微信小程序
(12, 37), -- 实战项目
(12, 49), -- 问题解答
(12, 50), -- 技术分享

-- 文章13: Linux服务器安全加固方案
(13, 21), -- Linux
(13, 38), -- 安全
(13, 49), -- 问题解答
(13, 50), -- 技术分享

-- 文章14: TypeScript在大型项目中的应用实践
(14, 15), -- TypeScript
(14, 37), -- 实战项目
(14, 50), -- 技术分享
(14, 52), -- 个人成长

-- 文章15: Kafka在订单系统中的应用
(15, 13), -- Kafka
(15, 32), -- 分布式系统
(15, 37), -- 实战项目
(15, 50), -- 技术分享

-- 文章16: 应届生选择Java还是Go？求前辈指点
(16, 14), -- Java
(16, 16), -- Go语言
(16, 47), -- 面试题
(16, 52), -- 职业规划

-- 文章17: 35岁程序员出路在哪里？
(17, 52), -- 职业规划
(17, 53), -- 个人成长
(17, 49), -- 问题解答

-- 文章18: 外包公司值得去吗？
(18, 47), -- 面试题
(18, 52), -- 职业规划
(18, 49), -- 问题解答

-- 文章19: 如何准备大厂算法面试？
(19, 46), -- 算法题
(19, 47), -- 面试题
(19, 48), -- LeetCode
(19, 52), -- 职业规划

-- 文章20: 转行学编程来得及吗？
(20, 52), -- 职业规划
(20, 53), -- 个人成长
(20, 49), -- 问题解答

-- 文章21: 考研还是直接工作？
(21, 52), -- 职业规划
(21, 49), -- 问题解答

-- 文章22: 前端现在是不是饱和了？
(22, 25), -- 前端
(22, 52), -- 职业规划
(22, 49), -- 问题解答

-- 文章23: 公司技术栈太老要不要离职？
(23, 52), -- 职业规划
(23, 53), -- 个人成长
(23, 49), -- 问题解答

-- 文章24: 该不该接私活？
(24, 52), -- 职业规划
(24, 49), -- 问题解答

-- 文章25: 如何快速熟悉一个大型代码库？
(25, 44), -- 代码规范
(25, 52), -- 职业规划
(25, 49), -- 问题解答

-- 文章26: 分享一下我的工作台配置
(26, 54), -- 资源推荐
(26, 55), -- 工具推荐
(26, 50), -- 技术分享

-- 文章27: 推荐几个提升效率的Chrome插件
(27, 54), -- 资源推荐
(27, 55), -- 工具推荐
(27, 50), -- 技术分享

-- 文章28: 我的书单：2023年读过的好书
(28, 54), -- 资源推荐
(28, 53), -- 个人成长
(28, 50), -- 技术分享

-- 文章29: 免费编程学习资源合集
(29, 54), -- 资源推荐
(29, 50), -- 技术分享

-- 文章30: 程序员健康指南：拯救你的颈椎
(30, 53), -- 个人成长
(30, 50), -- 技术分享

-- 文章31: 我的副业收入来源分享
(31, 52), -- 职业规划
(31, 53), -- 个人成长
(31, 50), -- 技术分享

-- 文章32: 开源项目维护经验谈
(32, 51), -- 开源项目
(32, 53), -- 个人成长
(32, 50), -- 技术分享

-- 文章33: 技术博客写作心得
(33, 53), -- 个人成长
(33, 50), -- 技术分享

-- 文章34: 远程工作一年体验
(34, 52), -- 职业规划
(34, 53), -- 个人成长
(34, 50), -- 技术分享

-- 文章35: 我的年度总结：2023技术成长
(35, 53), -- 个人成长
(35, 50), -- 技术分享

-- 文章36: WebSocket实现实时聊天功能
(36, 19), -- Node.js
(36, 25), -- 前端
(36, 37), -- 实战项目
(36, 50), -- 技术分享

-- 文章37: 前端错误监控系统搭建
(37, 25), -- 前端
(37, 37), -- 实战项目
(37, 38), -- 安全
(37, 50), -- 技术分享

-- 文章38: 微服务网关选型对比
(38, 4),  -- Spring Boot
(38, 33), -- 微服务
(38, 50), -- 技术分享

-- 文章39: Python爬虫反反爬策略
(39, 17), -- Python
(39, 37), -- 实战项目
(39, 50), -- 技术分享

-- 文章40: 移动端H5适配方案总结
(40, 25), -- 前端
(40, 50), -- 技术分享
(40, 48), -- 性能优化

-- 文章41: 数据库分库分表实践
(41, 14), -- MySQL优化
(41, 31), -- 数据库
(41, 32), -- 分布式系统
(41, 37), -- 实战项目

-- 文章42: Jenkins Pipeline自动化部署
(42, 22), -- Jenkins
(42, 37), -- 实战项目
(42, 50), -- 技术分享

-- 文章43: React性能优化实战
(43, 2),  -- React
(43, 48), -- 性能优化
(43, 50), -- 技术分享

-- 文章44: Nginx配置SSL证书
(44, 21), -- Nginx
(44, 38), -- 安全
(44, 50), -- 技术分享

-- 文章45: Elasticsearch入门教程
(45, 12), -- Elasticsearch
(45, 31), -- 数据库
(45, 50); -- 技术分享

-- 更新标签的统计计数
UPDATE article_tag tag
SET article_count = (
    SELECT COUNT(*)
    FROM article_tag_relation atr
    WHERE atr.tag_id = tag.id
);



-- 插入用户数据
INSERT INTO `user` (
    `username`, `password`, `email`, `phone`, `nickname`, `avatar`,
    `cover_images`, `gender`, `birthday`, `introduction`, `website`,
    `location`, `company`, `position`, `signature`, `score`, `level`,
    `experience`, `gold`, `topic_count`, `comment_count`, `follower_count`,
    `following_count`, `collection_count`, `status`, `email_verified`,
    `phone_verified`, `last_login_time`, `last_login_ip`
) VALUES
-- 管理员用户 (1-5)
('admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin@example.com', '13800000001', '超级管理员', 'https://example.com/avatars/admin.jpg', '["https://example.com/covers/admin1.jpg", "https://example.com/covers/admin2.jpg"]', '1', '1990-01-01', '系统管理员，负责社区管理', 'https://admin.blog.com', '北京', '深度求索', '系统架构师', '技术改变世界', 5000, 10, 10000, 1000, 150, 300, 5000, 200, 120, '0', '1', '1', '2024-03-15 14:30:00', '192.168.1.1'),
('superuser', 'e10adc3949ba59abbe56e057f20f883e', 'superuser@example.com', '13800000002', '超级用户', 'https://example.com/avatars/superuser.jpg', '["https://example.com/covers/super1.jpg"]', '1', '1992-05-15', '技术专家，热爱分享', 'https://superuser.dev', '上海', '腾讯科技', '高级开发工程师', '代码即艺术', 4200, 9, 8500, 800, 120, 250, 4200, 180, 90, '0', '1', '1', '2024-03-15 10:20:00', '192.168.1.2'),
('sysadmin', 'e10adc3949ba59abbe56e057f20f883e', 'sysadmin@example.com', '13800000003', '系统管理员', 'https://example.com/avatars/sysadmin.jpg', NULL, '1', '1988-11-22', '专注于系统运维', 'https://sysadmin.io', '深圳', '阿里巴巴', '运维总监', '稳定压倒一切', 3800, 8, 7200, 700, 80, 180, 3500, 150, 70, '0', '1', '1', '2024-03-14 16:45:00', '192.168.1.3'),
('webmaster', 'e10adc3949ba59abbe56e057f20f883e', 'webmaster@example.com', '13800000004', '网站管理员', 'https://example.com/avatars/webmaster.jpg', '["https://example.com/covers/web1.jpg", "https://example.com/covers/web2.jpg"]', '2', '1993-03-08', '前端开发者，UI/UX爱好者', 'https://webmaster.design', '杭州', '字节跳动', '前端专家', '设计让世界更美好', 3500, 7, 6800, 650, 95, 210, 3800, 220, 85, '0', '1', '1', '2024-03-15 09:15:00', '192.168.1.4'),
('moderator', 'e10adc3949ba59abbe56e057f20f883e', 'moderator@example.com', '13800000005', '内容版主', 'https://example.com/avatars/moderator.jpg', NULL, '2', '1995-07-30', '社区内容管理，维护良好氛围', 'https://moderator.blog', '广州', '网易', '内容运营', '分享知识，传播价值', 3000, 6, 6000, 600, 60, 150, 3000, 120, 60, '0', '1', '1', '2024-03-14 14:20:00', '192.168.1.5'),

-- 开发工程师 (6-20)
('coder_li', 'e10adc3949ba59abbe56e057f20f883e', 'coder_li@example.com', '13800000006', '李程序员', 'https://example.com/avatars/coder_li.jpg', '["https://example.com/covers/coder1.jpg"]', '1', '1994-02-14', 'Java后端开发，Spring Cloud专家', NULL, '北京', '京东', '高级Java开发', 'Talk is cheap, show me the code', 2800, 5, 5500, 550, 45, 120, 2500, 80, 40, '0', '1', '1', '2024-03-15 11:30:00', '192.168.1.6'),
('frontend_wang', 'e10adc3949ba59abbe56e057f20f883e', 'frontend_wang@example.com', '13800000007', '前端小王', 'https://example.com/avatars/frontend_wang.jpg', NULL, '1', '1996-09-25', 'React/Vue前端开发', 'https://wangfe.dev', '上海', '拼多多', '前端开发工程师', '让交互更流畅', 2500, 5, 5000, 500, 40, 100, 2200, 100, 35, '0', '1', '1', '2024-03-15 08:45:00', '192.168.1.7'),
('python_zhang', 'e10adc3949ba59abbe56e057f20f883e', 'python_zhang@example.com', '13800000008', 'Python老张', 'https://example.com/avatars/python_zhang.jpg', '["https://example.com/covers/python1.jpg"]', '1', '1990-12-05', 'Python全栈开发，AI方向', 'https://zhangpy.com', '深圳', '华为', 'Python开发专家', '人生苦短，我用Python', 3200, 6, 6200, 620, 55, 140, 2800, 90, 50, '0', '1', '1', '2024-03-14 20:15:00', '192.168.1.8'),
('go_dev', 'e10adc3949ba59abbe56e057f20f883e', 'go_dev@example.com', '13800000009', 'Go语言爱好者', 'https://example.com/avatars/go_dev.jpg', NULL, '1', '1993-04-18', 'Go微服务开发', NULL, '杭州', '蚂蚁集团', 'Go开发工程师', '简单、高效、并发', 2400, 4, 4800, 480, 35, 90, 2000, 70, 30, '0', '1', '1', '2024-03-15 13:20:00', '192.168.1.9'),
('mobile_zhao', 'e10adc3949ba59abbe56e057f20f883e', 'mobile_zhao@example.com', '13800000010', '移动端赵工', 'https://example.com/avatars/mobile_zhao.jpg', '["https://example.com/covers/mobile1.jpg", "https://example.com/covers/mobile2.jpg"]', '1', '1995-08-12', 'Flutter/React Native跨端开发', 'https://zhaomobile.com', '成都', '小米', '移动端开发', '一次编写，多端运行', 2100, 4, 4200, 420, 30, 80, 1800, 60, 25, '0', '1', '1', '2024-03-14 19:30:00', '192.168.1.10'),
('db_admin', 'e10adc3949ba59abbe56e057f20f883e', 'db_admin@example.com', '13800000011', '数据库管理员', 'https://example.com/avatars/db_admin.jpg', NULL, '2', '1991-06-28', 'MySQL/Redis性能优化', NULL, '南京', '中兴', 'DBA', '数据是企业的血液', 2900, 5, 5800, 580, 25, 60, 1500, 40, 20, '0', '1', '1', '2024-03-15 15:40:00', '192.168.1.11'),
('devops_chen', 'e10adc3949ba59abbe56e057f20f883e', 'devops_chen@example.com', '13800000012', '运维老陈', 'https://example.com/avatars/devops_chen.jpg', '["https://example.com/covers/devops1.jpg"]', '1', '1989-03-17', 'Docker/K8s/CI-CD', 'https://chenops.com', '西安', '荣耀', '运维工程师', '自动化一切', 2700, 5, 5400, 540, 20, 50, 1300, 30, 15, '0', '1', '1', '2024-03-14 17:50:00', '192.168.1.12'),
('ai_researcher', 'e10adc3949ba59abbe56e057f20f883e', 'ai_researcher@example.com', '13800000013', 'AI研究员', 'https://example.com/avatars/ai_researcher.jpg', NULL, '2', '1994-11-03', '机器学习/深度学习', 'https://airesearcher.ai', '武汉', '百度', 'AI算法工程师', '让机器更智能', 3300, 6, 6600, 660, 50, 110, 2600, 85, 45, '0', '1', '1', '2024-03-15 12:10:00', '192.168.1.13'),
('security_liu', 'e10adc3949ba59abbe56e057f20f883e', 'security_liu@example.com', '13800000014', '安全刘', 'https://example.com/avatars/security_liu.jpg', '["https://example.com/covers/security1.jpg"]', '1', '1992-07-19', '网络安全，渗透测试', NULL, '厦门', '美图', '安全工程师', '没有绝对的安全', 2200, 4, 4400, 440, 15, 40, 1100, 25, 10, '0', '1', '1', '2024-03-14 21:05:00', '192.168.1.14'),
('fullstack_sun', 'e10adc3949ba59abbe56e057f20f883e', 'fullstack_sun@example.com', '13800000015', '全栈孙', 'https://example.com/avatars/fullstack_sun.jpg', NULL, '1', '1996-01-22', '前后端都能干', 'https://sunfullstack.com', '长沙', '三一重工', '全栈工程师', '样样通，样样松', 2000, 3, 4000, 400, 35, 95, 1900, 65, 30, '0', '1', '1', '2024-03-15 07:30:00', '192.168.1.15'),

-- 更多普通用户 (21-50)
('test_user1', 'e10adc3949ba59abbe56e057f20f883e', 'test1@example.com', '13800000016', '测试用户1', NULL, NULL, '0', '1998-05-05', '测试账号1', NULL, '北京', NULL, NULL, NULL, 500, 2, 1000, 100, 5, 20, 100, 20, 5, '0', '0', '0', '2024-03-14 10:20:00', '192.168.1.16'),
('test_user2', 'e10adc3949ba59abbe56e057f20f883e', 'test2@example.com', '13800000017', '测试用户2', 'https://example.com/avatars/default.jpg', NULL, '2', '1997-08-15', '测试账号2', NULL, '上海', NULL, '实习生', '努力学习中', 300, 1, 600, 60, 2, 10, 50, 10, 2, '0', '0', '1', '2024-03-13 16:40:00', '192.168.1.17'),
('student_zhang', 'e10adc3949ba59abbe56e057f20f883e', 'student_zhang@example.com', '13800000018', '大学生小张', NULL, NULL, '1', '2000-03-10', '计算机专业在校生', NULL, '武汉', '武汉大学', '学生', '未来程序员', 150, 1, 300, 30, 1, 5, 30, 5, 1, '0', '1', '0', '2024-03-12 14:15:00', '192.168.1.18'),
('vip_user', 'e10adc3949ba59abbe56e057f20f883e', 'vip@example.com', '13800000019', 'VIP会员', 'https://example.com/avatars/vip.jpg', '["https://example.com/covers/vip1.jpg"]', '1', '1985-09-28', '付费会员，享受更多权益', 'https://vip.blog', '北京', '自由职业', '独立开发者', '知识付费拥护者', 4500, 8, 9000, 900, 70, 160, 3200, 140, 75, '0', '1', '1', '2024-03-15 18:20:00', '192.168.1.19'),
('content_creator', 'e10adc3949ba59abbe56e057f20f883e', 'creator@example.com', '13800000020', '内容创作者', 'https://example.com/avatars/creator.jpg', NULL, '2', '1993-12-12', '技术博客作者，Youtuber', 'https://creator.tech', '杭州', '自媒体', '内容创作者', '分享创造价值', 3800, 7, 7600, 760, 85, 190, 3500, 160, 80, '0', '1', '1', '2024-03-15 16:35:00', '192.168.1.20'),
('code_farmer', 'e10adc3949ba59abbe56e057f20f883e', 'codefarmer@example.com', '13800000021', '代码农民', NULL, NULL, '1', '1991-04-03', '勤勤恳恳写代码', NULL, '郑州', '富士康', '软件工程师', '码农的自我修养', 1800, 3, 3600, 360, 25, 70, 1400, 50, 20, '0', '1', '0', '2024-03-14 11:45:00', '192.168.1.21'),
('bug_maker', 'e10adc3949ba59abbe56e057f20f883e', 'bugmaker@example.com', '13800000022', 'Bug制造者', 'https://example.com/avatars/bug.jpg', NULL, '1', '1995-06-30', '专业制造Bug 20年', 'https://bug.life', '重庆', '待业', 'Bug工程师', '没有Bug的代码不是好代码', 800, 2, 1600, 160, 10, 30, 600, 15, 8, '0', '0', '0', '2024-03-13 09:20:00', '192.168.1.22'),
('tech_girl', 'e10adc3949ba59abbe56e057f20f883e', 'techgirl@example.com', '13800000023', '技术女孩', 'https://example.com/avatars/techgirl.jpg', '["https://example.com/covers/girl1.jpg"]', '2', '1997-02-14', '女程序员，打破偏见', 'https://techgirl.cn', '深圳', '腾讯', '前端开发', 'Girls can code!', 2200, 4, 4400, 440, 40, 90, 2000, 80, 35, '0', '1', '1', '2024-03-15 14:50:00', '192.168.1.23'),
('old_driver', 'e10adc3949ba59abbe56e057f20f883e', 'olddriver@example.com', '13800000024', '老司机', NULL, NULL, '1', '1982-11-11', '从业20年的技术老兵', NULL, '广州', '金山', '技术总监', '姜还是老的辣', 5000, 10, 10000, 1000, 100, 200, 4500, 180, 95, '0', '1', '1', '2024-03-15 20:30:00', '192.168.1.24'),
('newbie_coder', 'e10adc3949ba59abbe56e057f20f883e', 'newbie@example.com', '13800000025', '编程新手', 'https://example.com/avatars/newbie.jpg', NULL, '0', '2002-07-07', '刚学编程3个月', NULL, '哈尔滨', NULL, '学生', '路漫漫其修远兮', 100, 1, 200, 20, 0, 2, 10, 2, 0, '0', '0', '0', '2024-03-12 08:10:00', '192.168.1.25'),

-- 继续添加更多用户...
('java_master', 'e10adc3949ba59abbe56e057f20f883e', 'java_master@example.com', '13800000026', 'Java大师', 'https://example.com/avatars/java_master.jpg', NULL, '1', '1988-09-09', 'Java领域专家', 'https://javamaster.io', '北京', '京东', '首席架构师', 'Java是最好的语言', 4800, 9, 9600, 960, 120, 250, 5000, 200, 110, '0', '1', '1', '2024-03-15 19:15:00', '192.168.1.26'),
('python_fan', 'e10adc3949ba59abbe56e057f20f883e', 'python_fan@example.com', '13800000027', 'Python粉丝', NULL, NULL, '1', '1994-04-04', 'Python狂热爱好者', NULL, '上海', '携程', '数据工程师', '人生苦短，我用Python', 2600, 5, 5200, 520, 45, 100, 2300, 90, 45, '0', '1', '1', '2024-03-14 15:30:00', '192.168.1.27'),
('golang_dev', 'e10adc3949ba59abbe56e057f20f883e', 'golang_dev@example.com', '13800000028', 'Golang开发者', 'https://example.com/avatars/golang.jpg', NULL, '1', '1993-10-10', 'Go语言布道师', 'https://golang.dev', '深圳', '腾讯', '后台开发', 'Go语言改变世界', 2900, 5, 5800, 580, 50, 110, 2400, 95, 50, '0', '1', '1', '2024-03-15 13:45:00', '192.168.1.28'),
('rustacean', 'e10adc3949ba59abbe56e057f20f883e', 'rustacean@example.com', '13800000029', 'Rustaceans', NULL, NULL, '1', '1996-06-06', 'Rust语言爱好者', NULL, '杭州', '阿里云', '系统开发', '安全与性能的平衡', 2100, 4, 4200, 420, 30, 70, 1700, 60, 30, '0', '1', '1', '2024-03-14 18:20:00', '192.168.1.29'),
('frontend_queen', 'e10adc3949ba59abbe56e057f20f883e', 'frontend_queen@example.com', '13800000030', '前端女王', 'https://example.com/avatars/queen.jpg', '["https://example.com/covers/queen1.jpg"]', '2', '1995-03-03', '前端技术专家，React核心贡献者', 'https://frontendqueen.com', '北京', '字节跳动', '前端架构师', '细节决定成败', 4200, 8, 8400, 840, 90, 180, 3800, 150, 85, '0', '1', '1', '2024-03-15 17:40:00', '192.168.1.30'),
('backend_king', 'e10adc3949ba59abbe56e057f20f883e', 'backend_king@example.com', '13800000031', '后端之王', NULL, NULL, '1', '1989-12-12', '后端架构专家', NULL, '上海', '美团', '后端架构师', '稳定、高效、可扩展', 4600, 9, 9200, 920, 110, 220, 4200, 170, 100, '0', '1', '1', '2024-03-15 21:10:00', '192.168.1.31'),
('devops_master', 'e10adc3949ba59abbe56e057f20f883e', 'devops_master@example.com', '13800000032', 'DevOps大师', 'https://example.com/avatars/devops_master.jpg', NULL, '1', '1987-07-07', '自动化运维专家', 'https://devops.master', '深圳', '华为', 'DevOps工程师', '自动化一切，监控一切', 3400, 7, 6800, 680, 40, 90, 2000, 70, 40, '0', '1', '1', '2024-03-14 22:15:00', '192.168.1.32'),
('data_scientist', 'e10adc3949ba59abbe56e057f20f883e', 'data_scientist@example.com', '13800000033', '数据科学家', NULL, NULL, '2', '1992-08-08', '数据挖掘与机器学习', NULL, '北京', '百度', '数据科学家', '数据驱动决策', 3700, 7, 7400, 740, 60, 130, 2800, 100, 60, '0', '1', '1', '2024-03-15 11:55:00', '192.168.1.33'),
('cyber_security', 'e10adc3949ba59abbe56e057f20f883e', 'cyber_security@example.com', '13800000034', '网络安全专家', 'https://example.com/avatars/security.jpg', NULL, '1', '1985-05-05', '网络安全，渗透测试', 'https://security.expert', '上海', '360', '安全专家', '攻防之道', 3100, 6, 6200, 620, 35, 80, 1900, 65, 35, '0', '1', '1', '2024-03-14 19:45:00', '192.168.1.34'),
('blockchain_dev', 'e10adc3949ba59abbe56e057f20f883e', 'blockchain@example.com', '13800000035', '区块链开发者', NULL, NULL, '1', '1994-01-01', '区块链、智能合约', NULL, '杭州', '蚂蚁链', '区块链工程师', '去中心化的未来', 2300, 4, 4600, 460, 30, 70, 1600, 55, 25, '0', '1', '1', '2024-03-13 20:30:00', '192.168.1.35'),
('game_developer', 'e10adc3949ba59abbe56e057f20f883e', 'game_dev@example.com', '13800000036', '游戏开发者', 'https://example.com/avatars/game.jpg', NULL, '1', '1993-11-11', '游戏开发，Unity3D', 'https://game.dev', '广州', '网易游戏', '游戏开发工程师', '创造虚拟世界', 1900, 3, 3800, 380, 25, 60, 1400, 45, 20, '0', '1', '1', '2024-03-14 12:35:00', '192.168.1.36'),
('mobile_expert', 'e10adc3949ba59abbe56e057f20f883e', 'mobile_expert@example.com', '13800000037', '移动端专家', NULL, NULL, '2', '1996-02-02', 'iOS/Android开发', NULL, '北京', '小米', '移动端负责人', '移动优先', 2700, 5, 5400, 540, 45, 100, 2200, 85, 40, '0', '1', '1', '2024-03-15 15:25:00', '192.168.1.37'),
('cloud_architect', 'e10adc3949ba59abbe56e057f20f883e', 'cloud@example.com', '13800000038', '云架构师', 'https://example.com/avatars/cloud.jpg', NULL, '1', '1986-04-04', '云计算架构设计', 'https://cloud.arch', '深圳', '腾讯云', '云架构师', '云原生，未来已来', 4300, 8, 8600, 860, 70, 150, 3100, 120, 70, '0', '1', '1', '2024-03-15 22:05:00', '192.168.1.38'),
('ai_engineer', 'e10adc3949ba59abbe56e057f20f883e', 'ai_engineer@example.com', '13800000039', 'AI工程师', NULL, NULL, '1', '1991-09-09', '机器学习工程化', NULL, '北京', '字节跳动', 'AI工程师', 'AI赋能产业', 3500, 7, 7000, 700, 55, 120, 2500, 95, 55, '0', '1', '1', '2024-03-14 21:50:00', '192.168.1.39'),
('open_source', 'e10adc3949ba59abbe56e057f20f883e', 'opensource@example.com', '13800000040', '开源贡献者', 'https://example.com/avatars/opensource.jpg', NULL, '2', '1995-12-12', '开源项目维护者', 'https://github.com/opensource', '杭州', 'GitHub', '开源倡导者', '开源改变世界', 3900, 7, 7800, 780, 80, 170, 3300, 140, 80, '0', '1', '1', '2024-03-15 20:45:00', '192.168.1.40'),

-- 最后10个普通用户
('user_41', 'e10adc3949ba59abbe56e057f20f883e', 'user41@example.com', '13800000041', '用户41', NULL, NULL, '0', '1998-06-18', '普通用户', NULL, '南京', NULL, NULL, NULL, 400, 2, 800, 80, 8, 25, 200, 30, 8, '0', '0', '0', '2024-03-12 13:20:00', '192.168.1.41'),
('user_42', 'e10adc3949ba59abbe56e057f20f883e', 'user42@example.com', '13800000042', '用户42', NULL, NULL, '1', '1997-04-22', '普通用户', NULL, '天津', NULL, NULL, NULL, 350, 2, 700, 70, 6, 20, 150, 25, 6, '0', '0', '0', '2024-03-11 15:30:00', '192.168.1.42'),
('user_43', 'e10adc3949ba59abbe56e057f20f883e', 'user43@example.com', '13800000043', '用户43', NULL, NULL, '2', '1999-08-30', '普通用户', NULL, '苏州', NULL, NULL, NULL, 300, 1, 600, 60, 4, 15, 120, 20, 4, '0', '0', '0', '2024-03-10 10:45:00', '192.168.1.43'),
('user_44', 'e10adc3949ba59abbe56e057f20f883e', 'user44@example.com', '13800000044', '用户44', NULL, NULL, '1', '2000-01-15', '普通用户', NULL, '宁波', NULL, NULL, NULL, 250, 1, 500, 50, 3, 10, 100, 15, 3, '0', '0', '0', '2024-03-09 16:55:00', '192.168.1.44'),
('user_45', 'e10adc3949ba59abbe56e057f20f883e', 'user45@example.com', '13800000045', '用户45', NULL, NULL, '0', '1996-11-08', '普通用户', NULL, '合肥', NULL, NULL, NULL, 200, 1, 400, 40, 2, 8, 80, 10, 2, '0', '0', '0', '2024-03-08 09:10:00', '192.168.1.45'),
('user_46', 'e10adc3949ba59abbe56e057f20f883e', 'user46@example.com', '13800000046', '用户46', NULL, NULL, '2', '1995-03-25', '普通用户', NULL, '福州', NULL, NULL, NULL, 150, 1, 300, 30, 1, 5, 60, 8, 1, '0', '0', '0', '2024-03-07 14:25:00', '192.168.1.46'),
('user_47', 'e10adc3949ba59abbe56e057f20f883e', 'user47@example.com', '13800000047', '用户47', NULL, NULL, '1', '1994-07-14', '普通用户', NULL, '济南', NULL, NULL, NULL, 100, 1, 200, 20, 0, 3, 40, 5, 0, '0', '0', '0', '2024-03-06 11:35:00', '192.168.1.47'),
('user_48', 'e10adc3949ba59abbe56e057f20f883e', 'user48@example.com', '13800000048', '用户48', NULL, NULL, '2', '1993-09-19', '普通用户', NULL, '青岛', NULL, NULL, NULL, 50, 1, 100, 10, 0, 1, 20, 3, 0, '0', '0', '0', '2024-03-05 17:40:00', '192.168.1.48'),
('user_49', 'e10adc3949ba59abbe56e057f20f883e', 'user49@example.com', '13800000049', '用户49', NULL, NULL, '1', '1992-12-31', '普通用户', NULL, '大连', NULL, NULL, NULL, 20, 1, 40, 4, 0, 0, 10, 1, 0, '0', '0', '0', '2024-03-04 08:50:00', '192.168.1.49'),
('user_50', 'e10adc3949ba59abbe56e057f20f883e', 'user50@example.com', '13800000050', '用户50', NULL, NULL, '0', '1991-05-17', '最后一个用户', NULL, '沈阳', NULL, NULL, '我是最后一个', 10, 1, 20, 2, 0, 0, 5, 0, 0, '0', '0', '0', '2024-03-03 12:15:00', '192.168.1.50');

INSERT INTO dictionary (dict_type, dict_value, description, dict_sort) VALUES
-- 通知类型
('notification_type', 'system', '系统消息', 1),
('notification_type', 'user', '用户互动', 2),
('notification_type', 'message', '私信提醒', 3),

-- 动作类型
('action_type', 'like_article', '点赞文章', 1),
('action_type', 'like_comment', '点赞评论', 2),
('action_type', 'comment_article', '评论文章', 3),
('action_type', 'reply_comment', '回复评论', 4),
('action_type', 'follow_user', '关注用户', 5),
('action_type', 'collect_article', '收藏文章', 6),
('action_type', 'share_article', '分享文章', 7),
('action_type', 'mention_user', '@提到用户', 8),
('action_type', 'private_message', '私信消息', 9),
('action_type', 'article_published', '文章发布成功', 10),
('action_type', 'comment_liked', '评论被点赞', 11),
('action_type', 'daily_signin', '每日签到', 12),
('action_type', 'level_up', '等级提升', 13),

-- 私信内容类型
('message_type', 'text', '文本消息', 1),
('message_type', 'image', '图片消息', 2),
('message_type', 'file', '文件消息', 3),
('message_type', 'link', '链接消息', 4);

INSERT INTO user_privacy (user_id, email_visibility, phone_visibility, profile_visibility, can_comment, article_visibility) VALUES
(1, '0', '0', '0', '0', '0'),
(2, '0', '0', '0', '0', '0'),
(3, '0', '0', '0', '0', '0'),
(4, '0', '0', '0', '0', '0'),
(5, '0', '0', '0', '0', '0'),
(6, '0', '0', '0', '0', '0'),
(7, '0', '0', '0', '0', '0'),
(8, '0', '0', '0', '0', '0'),
(9, '0', '0', '0', '0', '0'),
(10, '0', '0', '0', '0', '0'),
(11, '0', '0', '0', '0', '0'),
(12, '0', '0', '0', '0', '0'),
(13, '0', '0', '0', '0', '0'),
(14, '0', '0', '0', '0', '0'),
(15, '0', '0', '0', '0', '0'),
(16, '0', '0', '0', '0', '0'),
(17, '0', '0', '0', '0', '0'),
(18, '0', '0', '0', '0', '0'),
(19, '0', '0', '0', '0', '0'),
(20, '0', '0', '0', '0', '0'),
(21, '0', '0', '0', '0', '0'),
(22, '0', '0', '0', '0', '0'),
(23, '0', '0', '0', '0', '0'),
(24, '0', '0', '0', '0', '0'),
(25, '0', '0', '0', '0', '0'),
(26, '0', '0', '0', '0', '0'),
(27, '0', '0', '0', '0', '0'),
(28, '0', '0', '0', '0', '0'),
(29, '0', '0', '0', '0', '0'),
(30, '0', '0', '0', '0', '0'),
(31, '0', '0', '0', '0', '0'),
(32, '0', '0', '0', '0', '0'),
(33, '0', '0', '0', '0', '0'),
(34, '0', '0', '0', '0', '0'),
(35, '0', '0', '0', '0', '0'),
(36, '0', '0', '0', '0', '0'),
(37, '0', '0', '0', '0', '0'),
(38, '0', '0', '0', '0', '0'),
(39, '0', '0', '0', '0', '0'),
(40, '0', '0', '0', '0', '0'),
(41, '0', '0', '0', '0', '0'),
(42, '0', '0', '0', '0', '0'),
(43, '0', '0', '0', '0', '0'),
(44, '0', '0', '0', '0', '0'),
(45, '0', '0', '0', '0', '0'),
(46, '0', '0', '0', '0', '0'),
(47, '0', '0', '0', '0', '0'),
(48, '0', '0', '0', '0', '0'),
(49, '0', '0', '0', '0', '0'),
(50, '0', '0', '0', '0', '0');



INSERT INTO report (reporter_id, target_type, target_id, target_user_id, reason_type, reason_detail, evidence_urls, status, admin_id, admin_remark, processed_at, created_at, updated_at) VALUES
-- 待处理的文章举报
(5, 'article', 3, 1, 'inappropriate', '文章内容含有不当言论，攻击性语言', 'http://example.com/evidence1.jpg,http://example.com/evidence2.jpg', 'pending', NULL, NULL, NULL, '2024-01-15 10:23:45', '2024-01-15 10:23:45'),
(8, 'article', 8, 1, 'spam', '明显的广告推广内容，与技术无关', 'http://example.com/evidence3.jpg', 'pending', NULL, NULL, NULL, '2024-01-16 14:32:21', '2024-01-16 14:32:21'),
(12, 'article', 16, 1, 'harassment', '文章内容人身攻击其他用户', 'http://example.com/evidence4.jpg,http://example.com/evidence5.jpg', 'pending', NULL, NULL, NULL, '2024-01-18 09:15:33', '2024-01-18 09:15:33'),
(15, 'article', 19, 1, 'copyright', '抄袭我的原创文章，多处内容雷同', 'http://example.com/evidence6.jpg,http://example.com/evidence7.jpg,http://example.com/evidence8.jpg', 'processing', 2, '已联系原作者核实中', NULL, '2024-01-20 11:42:17', '2024-01-20 15:30:22'),
(3, 'article', 22, 1, 'other', '标题与内容不符，标题党', 'http://example.com/evidence9.jpg', 'processing', 2, '等待内容审核', NULL, '2024-01-21 16:28:49', '2024-01-21 17:10:05'),
(7, 'article', 25, 2, 'spam', '重复发布相同内容', 'http://example.com/evidence10.jpg', 'resolved', 1, '已删除重复内容，警告用户', '2024-01-10 13:45:12', '2024-01-09 22:18:30', '2024-01-10 13:45:12'),
(11, 'article', 27, 2, 'inappropriate', '包含敏感词汇', 'http://example.com/evidence11.jpg', 'resolved', 1, '内容已修改', '2024-01-12 09:30:45', '2024-01-11 19:52:14', '2024-01-12 09:30:45'),
(14, 'article', 29, 2, 'harassment', '针对特定群体的攻击言论', 'http://example.com/evidence12.jpg', 'resolved', 2, '已删除该评论', '2024-01-14 10:22:33', '2024-01-13 08:45:21', '2024-01-14 10:22:33'),
(18, 'article', 31, 3, 'copyright', '未经授权转载我的文章', 'http://example.com/evidence13.jpg,http://example.com/evidence14.jpg', 'resolved', 1, '已下架侵权内容', '2024-01-05 14:18:47', '2024-01-04 23:10:56', '2024-01-05 14:18:47'),
(20, 'article', 33, 3, 'other', '内容质量太差，误导新手', 'http://example.com/evidence15.jpg', 'rejected', 2, '经审核不构成违规', '2024-01-08 16:42:19', '2024-01-07 12:34:28', '2024-01-08 16:42:19'),
(22, 'article', 35, 3, 'spam', '明显的引流广告', 'http://example.com/evidence16.jpg', 'rejected', 1, '证据不足', '2024-01-17 11:23:45', '2024-01-16 20:15:37', '2024-01-17 11:23:45'),
(24, 'article', 38, 4, 'inappropriate', '内容低俗', 'http://example.com/evidence17.jpg', 'rejected', 2, '未发现违规内容', '2024-01-19 15:32:18', '2024-01-18 18:44:52', '2024-01-19 15:32:18'),

-- 评论举报数据 (target_type = 'comment')
(2, 'comment', 5, 7, 'harassment', '评论中辱骂他人', 'http://example.com/evidence18.jpg', 'pending', NULL, NULL, NULL, '2024-01-22 08:15:23', '2024-01-22 08:15:23'),
(4, 'comment', 8, 9, 'spam', '重复刷屏评论', 'http://example.com/evidence19.jpg', 'pending', NULL, NULL, NULL, '2024-01-22 10:42:37', '2024-01-22 10:42:37'),
(6, 'comment', 12, 11, 'inappropriate', '包含不当言论', 'http://example.com/evidence20.jpg', 'processing', 1, '审核中', NULL, '2024-01-21 14:23:56', '2024-01-21 15:10:22'),
(9, 'comment', 15, 13, 'other', '引战评论', 'http://example.com/evidence21.jpg', 'processing', 2, '需要进一步核实', NULL, '2024-01-20 17:38:41', '2024-01-20 18:22:15'),
(13, 'comment', 18, 15, 'copyright', '评论中贴了我的原创代码未注明出处', 'http://example.com/evidence22.jpg', 'resolved', 1, '已添加引用说明', '2024-01-15 11:27:34', '2024-01-14 22:19:47', '2024-01-15 11:27:34'),
(16, 'comment', 21, 17, 'harassment', '恶意攻击作者', 'http://example.com/evidence23.jpg', 'resolved', 2, '评论已删除', '2024-01-13 15:48:22', '2024-01-12 19:33:18', '2024-01-13 15:48:22'),
(19, 'comment', 24, 19, 'spam', '广告评论', 'http://example.com/evidence24.jpg', 'resolved', 1, '已处理', '2024-01-11 09:55:41', '2024-01-10 21:27:33', '2024-01-11 09:55:41'),
(21, 'comment', 27, 21, 'inappropriate', '涉政言论', 'http://example.com/evidence25.jpg', 'rejected', 2, '未发现违规', '2024-01-09 14:12:28', '2024-01-08 16:48:52', '2024-01-09 14:12:28'),

-- 用户举报数据 (target_type = 'user')
(1, 'user', 5, 5, 'spam', '用户持续发布广告', 'http://example.com/evidence26.jpg,http://example.com/evidence27.jpg', 'pending', NULL, NULL, NULL, '2024-01-23 09:33:42', '2024-01-23 09:33:42'),
(3, 'user', 8, 8, 'harassment', '该用户在多个评论区骚扰他人', 'http://example.com/evidence28.jpg,http://example.com/evidence29.jpg,http://example.com/evidence30.jpg', 'processing', 1, '正在查看用户历史记录', NULL, '2024-01-22 11:25:38', '2024-01-22 13:40:19'),
(7, 'user', 12, 12, 'inappropriate', '用户头像和昵称违规', 'http://example.com/evidence31.jpg', 'processing', 2, '已通知用户修改', NULL, '2024-01-21 18:52:14', '2024-01-22 09:15:47'),
(10, 'user', 16, 16, 'other', '疑似机器人账号', 'http://example.com/evidence32.jpg', 'resolved', 1, '已限制账号功能', '2024-01-18 10:38:26', '2024-01-17 20:44:13', '2024-01-18 10:38:26'),
(14, 'user', 20, 20, 'spam', '私信发送广告', 'http://example.com/evidence33.jpg', 'resolved', 2, '已警告用户', '2024-01-16 14:27:45', '2024-01-15 23:18:32', '2024-01-16 14:27:45'),
(17, 'user', 23, 23, 'harassment', '持续恶意评论', 'http://example.com/evidence34.jpg,http://example.com/evidence35.jpg', 'resolved', 1, '禁言7天', '2024-01-12 16:43:19', '2024-01-11 22:51:08', '2024-01-12 16:43:19'),
(20, 'user', 26, 26, 'inappropriate', '发布虚假信息', 'http://example.com/evidence36.jpg', 'rejected', 2, '证据不足', '2024-01-10 13:22:47', '2024-01-09 19:37:24', '2024-01-10 13:22:47'),
(23, 'user', 29, 29, 'copyright', '盗用他人头像和简介', 'http://example.com/evidence37.jpg', 'rejected', 1, '不构成侵权', '2024-01-08 11:54:33', '2024-01-07 18:42:16', '2024-01-08 11:54:33'),

-- 批量处理一些历史举报数据（2023年12月）
(4, 'article', 1, 1, 'spam', '垃圾广告文章', NULL, 'resolved', 1, '已处理', '2023-12-05 10:22:33', '2023-12-04 15:43:22', '2023-12-05 10:22:33'),
(6, 'article', 2, 1, 'inappropriate', '内容不适宜', 'http://example.com/old1.jpg', 'resolved', 2, '已修改', '2023-12-08 14:35:47', '2023-12-07 09:28:15', '2023-12-08 14:35:47'),
(9, 'comment', 3, 3, 'harassment', '辱骂他人', 'http://example.com/old2.jpg', 'resolved', 1, '已删除评论', '2023-12-12 11:48:29', '2023-12-11 20:17:43', '2023-12-12 11:48:29'),
(11, 'user', 7, 7, 'spam', '批量发广告', 'http://example.com/old3.jpg,http://example.com/old4.jpg', 'resolved', 2, '已封号', '2023-12-15 16:32:18', '2023-12-14 22:55:41', '2023-12-15 16:32:18'),
(13, 'article', 10, 1, 'copyright', '侵权转载', 'http://example.com/old5.jpg', 'resolved', 1, '已下架', '2023-12-18 09:17:54', '2023-12-17 13:42:36', '2023-12-18 09:17:54'),
(16, 'comment', 14, 14, 'inappropriate', '敏感言论', 'http://example.com/old6.jpg', 'resolved', 2, '已处理', '2023-12-22 14:28:45', '2023-12-21 18:33:27', '2023-12-22 14:28:45'),
(19, 'user', 18, 18, 'other', '恶意注册', 'http://example.com/old7.jpg', 'rejected', 1, '正常用户', '2023-12-25 10:55:32', '2023-12-24 21:19:48', '2023-12-25 10:55:32'),
(22, 'article', 23, 2, 'spam', '引流广告', 'http://example.com/old8.jpg', 'resolved', 2, '已删除', '2023-12-28 13:42:19', '2023-12-27 16:37:54', '2023-12-28 13:42:19'),
(24, 'comment', 26, 25, 'harassment', '攻击他人', 'http://example.com/old9.jpg', 'resolved', 1, '已禁言', '2023-12-30 15:33:47', '2023-12-29 19:28:15', '2023-12-30 15:33:47');


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

INSERT INTO system_config_group (group_name, group_label, sort) VALUES
('system','基础设置',1),
('feature','功能设置',2),
('upload','上传设置',3),
('security','安全设置',4),
('email','邮件设置',5);
INSERT INTO system_config (config_key,config_value,value_type,group_name,config_name,is_public,sort) VALUES
('site_name','程序员社区','string','system','网站名称',1,1),
('site_description','一个技术分享社区','string','system','网站描述',1,2),
('site_keywords','编程,技术,社区','string','system','网站关键词',1,3),
('site_logo','/static/logo.png','string','system','网站Logo',1,4),
('icp_number','','string','system','ICP备案号',1,5),
('contact_email','contact@example.com','string','system','联系邮箱',1,6);
INSERT INTO system_config VALUES
(NULL,'enable_register','true','boolean','feature','允许注册','是否允许新用户注册',1,1,'1',NOW(),NOW()),
(NULL,'enable_email_verify','false','boolean','feature','邮箱验证','注册是否需要邮箱验证',1,2,'1',NOW(),NOW()),
(NULL,'enable_comment','true','boolean','feature','评论功能','是否开启评论',1,3,'1',NOW(),NOW()),
(NULL,'comment_need_audit','false','boolean','feature','评论审核','评论是否需要审核',1,4,'1',NOW(),NOW()),
(NULL,'article_need_audit','false','boolean','feature','文章审核','文章发布是否需要审核',1,5,'1',NOW(),NOW()),
(NULL,'allow_guest_view','true','boolean','feature','游客访问','未登录是否可浏览',1,6,'1',NOW(),NOW());
INSERT INTO system_config VALUES
(NULL,'password_min_length','8','number','security','密码最小长度','密码长度限制',0,1,'1',NOW(),NOW()),
(NULL,'login_fail_limit','5','number','security','登录失败限制','失败多少次锁定',0,2,'1',NOW(),NOW()),
(NULL,'lock_duration','15','number','security','锁定时长','单位分钟',0,3,'1',NOW(),NOW()),
(NULL,'session_timeout','120','number','security','Session超时','单位分钟',0,4,'1',NOW(),NOW()),
(NULL,'enable_captcha','true','boolean','security','验证码','登录是否需要验证码',0,5,'1',NOW(),NOW()),
(NULL,'ip_blacklist','','string','security','IP黑名单','每行一个IP',0,6,'1',NOW(),NOW());
INSERT INTO system_config VALUES
(NULL,'mail_host','smtp.qq.com','string','email','SMTP服务器','邮件服务器地址',0,1,'1',NOW(),NOW()),
(NULL,'mail_port','465','number','email','SMTP端口','端口号',0,2,'1',NOW(),NOW()),
(NULL,'mail_username','','string','email','邮箱账号','发件邮箱',0,3,'1',NOW(),NOW()),
(NULL,'mail_password','','string','email','邮箱授权码','敏感信息',0,4,'1',NOW(),NOW()),
(NULL,'mail_from','','string','email','发件人','显示名称',0,5,'1',NOW(),NOW()),
(NULL,'mail_ssl','true','boolean','email','SSL','是否启用SSL',0,6,'1',NOW(),NOW());