create table T_LOGIN_LOG
(
    id          bigint primary key auto_increment comment '自增主键',
    user_id     bigint      not null comment '用户id',
    operation   tinyint     not null comment '操作类型（0未知，1登入，2登出，3撤销）',
    ip          varchar(20) not null comment 'ip地址',
    region      varchar(50) default null comment '地区',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    index idx_login_log_create_time (create_time desc)
) comment '登录日志表';