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


INSERT INTO T_JOB_VARIABLE (id, name, `value`, status)
VALUES (1, 'now', '#now()', 0);
INSERT INTO T_JOB_VARIABLE (id, name, `value`, status)
VALUES (2, 'now_format', '#now_format("yyyy-MM-dd")', 0);
INSERT INTO T_JOB_VARIABLE (id, name, `value`, status)
VALUES (3, 'add_month', '#add_month(#now(),-1)', 0);
INSERT INTO T_JOB_VARIABLE (id, name, `value`, status)
VALUES (4, 'add_month_format', '#add_month_format(#now(),-1,"yyyy-MM-dd")', 0);
INSERT INTO T_JOB_VARIABLE (id, name, `value`, status)
VALUES (5, 'add_day', '#add_day(#now(),-1)', 0);
INSERT INTO T_JOB_VARIABLE (id, name, `value`, status)
VALUES (6, 'add_day_format', '#add_day_format(#now(),-1,"yyyy-MM-dd")', 0);
INSERT INTO T_JOB_VARIABLE (id, name, `value`, status)
VALUES (7, 'add_hour', '#add_hour(#now(),-1)', 0);
INSERT INTO T_JOB_VARIABLE (id, name, `value`, status)
VALUES (8, 'add_hour_format', '#add_hour_format(#now(),-1,"yyyy-MM-dd HH")', 0);
INSERT INTO T_JOB_VARIABLE (id, name, `value`, status)
VALUES (9, 'add_minute', '#add_minute(#now(),-1)', 0);
INSERT INTO T_JOB_VARIABLE (id, name, `value`, status)
VALUES (10, 'add_minute_format', '#add_minute_format(#now(),-1,"yyyy-MM-dd HH:mm")', 0);