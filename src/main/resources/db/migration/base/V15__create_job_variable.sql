create table T_JOB_VARIABLE
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(30)  not null comment '变量名',
    `value`     varchar(100) not null comment '变量值(支持SPEL表达式)',
    status      bit(1)      default 0 comment '是否启用：0-禁用，1-启用',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_variable_name (name)
) comment = '任务变量表';