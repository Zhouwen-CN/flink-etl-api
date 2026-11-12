create table T_AI_CONVERSATION
(
    id          bigint primary key auto_increment comment '自增主键',
    user_id     bigint       not null comment '用户id',
    title       varchar(255) not null comment '会话标题',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间'
) comment 'AI会话';