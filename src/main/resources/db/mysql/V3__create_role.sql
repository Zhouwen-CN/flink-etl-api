create table T_ROLE
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(20)  not null comment '角色名称',
    remark      varchar(100) not null comment '角色描述',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_role_name (name)
) comment '角色表';

insert into T_ROLE(id, name, remark)
values (1, 'admin', '管理员');
insert into T_ROLE(id, name, remark)
values (2, 'dev', '开发');
insert into T_ROLE(id, name, remark)
values (3, 'test', '测试');

create table T_USER_ROLE
(
    id          bigint primary key auto_increment comment '自增主键',
    user_id     bigint not null comment '用户id',
    role_id     bigint not null comment '角色id',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_user_role (user_id, role_id)
) comment '用户角色关系表';

insert into T_USER_ROLE(id, role_id, user_id)
values (1, 1, 1);
insert into T_USER_ROLE(id, role_id, user_id)
values (2, 2, 2);
