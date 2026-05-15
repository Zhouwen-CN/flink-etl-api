create table T_USER
(
    id          bigint primary key auto_increment comment '自增主键',
    username    varchar(30) not null comment '用户名',
    password varchar(30) not null comment '密码',
    nickname varchar(30) not null comment '用户昵称',
    gender      tinyint     default 0 comment '用户性别(0未知 1男 2女)',
    status   bit(1) default 0 comment '账号状态(1启用 0停用)',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_user_name (username)
) comment '用户表';

-- admin 密码：admin123
-- zhangsan 密码：zhangsan123
insert into T_USER(id, username, password, nickname, gender, status)
values (1, 'admin', 'EnC/6IK+x2W2KNDMckXlmg==', '超级管理员', 0, 1);
insert into T_USER(id, username, password, nickname, gender, status)
values (2, 'zhangsan', 'FpbV8C6ZJxqGkDGUqYNqkQ==', '张三', 1, 1);