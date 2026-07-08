create table T_ALERT
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(50) not null comment '告警名称',
    email       varchar(50) not null comment '邮件地址',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_alert_name (name)
) comment '告警表';


create table T_ALERT_JOB
(
    id          bigint primary key auto_increment comment '自增主键',
    alert_id    bigint not null comment '告警id',
    job_id      bigint not null comment '任务id',
    send_time datetime default null comment '发送时间',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_alert_job_id (alert_id, job_id)
) comment '告警任务关系表';