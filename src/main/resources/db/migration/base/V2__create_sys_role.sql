create table T_SYS_ROLE
(
    id          bigint primary key auto_increment comment '自增主键',
    name       varchar(30) not null comment '角色名称',
    code       varchar(30) not null comment '角色标识符',
    data_scope tinyint default 2 comment '数据范围（1全部，2本部门及以下，3本部门）',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_sys_role_name (code)
) comment '角色表';

insert into T_SYS_ROLE(id, name, code, data_scope)
values (1, '管理员', 'admin', 1);
insert into T_SYS_ROLE(id, name, code)
values (2, '开发', 'dev');

create table T_SYS_USER_ROLE
(
    id          bigint primary key auto_increment comment '自增主键',
    user_id     bigint not null comment '用户id',
    role_id     bigint not null comment '角色id',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_sys_user_role (user_id, role_id)
) comment '用户角色关系表';

insert into T_SYS_USER_ROLE(id, role_id, user_id)
values (1, 1, 1);
insert into T_SYS_USER_ROLE(id, role_id, user_id)
values (2, 2, 2);
