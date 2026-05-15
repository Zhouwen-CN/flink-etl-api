create table T_ERROR_LOG
(
    id          bigint primary key auto_increment comment '自增主键',
    url         varchar(50) not null comment '请求地址',
    method      varchar(10) not null comment '请求方式',
    ip          varchar(20) not null comment 'ip地址',
    region      varchar(50)   default null comment '地区',
    error_msg   varchar(1000) default null comment '错误信息',
    create_user varchar(30)   default null comment '用户名',
    create_time timestamp     default CURRENT_TIMESTAMP comment '创建时间',
    index idx_error_log_create_time (create_time desc)
) comment '错误日志表';