create table t_user
(
    id          bigint primary key auto_increment comment '用户ID',
    username    varchar(30) not null comment '用户名',
    password    varchar(60) not null comment '密码',
    sex         tinyint     default 0 comment '用户性别(0未知 1男 2女)',
    avatar      text        default null comment '用户头像(base64)',
    is_enable   bit(1)      default 0 comment '账号状态(1启用 0停用)',
    login_ip    varchar(32) default null comment '最后登录IP',
    login_date  datetime    default null comment '最后登录时间',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间'
)
    comment '用户表';


insert into t_user(username, password, sex, is_enable)
values ('admin1', 'admin', 1, 1),
       ('admin2', 'admin', 1, 1),
       ('admin3', 'admin', 1, 1),
       ('admin4', 'admin', 1, 1),
       ('admin5', 'admin', 1, 1),
       ('admin6', 'admin', 1, 1),
       ('admin7', 'admin', 1, 1),
       ('admin8', 'admin', 1, 1),
       ('admin9', 'admin', 1, 1),
       ('admin10', 'admin', 1, 1),
       ('admin11', 'admin', 1, 1),
       ('admin12', 'admin', 1, 1),
       ('admin13', 'admin', 1, 1),
       ('admin14', 'admin', 1, 1),
       ('admin15', 'admin', 1, 1);