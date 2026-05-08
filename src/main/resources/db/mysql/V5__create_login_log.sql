create table T_LOGIN_LOG
(
    id          bigint primary key auto_increment comment '自增主键',
    username  varchar(30) not null comment '用户名',
    operation tinyint     not null comment '操作类型（1登入 2登出 3撤销）',
    ip          varchar(20) not null comment 'ip地址',
    region      varchar(50) default null comment '地区',
    status    bit(1)      not null comment '0失败 1成功',
    remark    varchar(50) default null comment '备注',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    index idx_login_log_create_time (create_time desc)
) comment '登录日志表';