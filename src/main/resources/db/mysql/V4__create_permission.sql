CREATE TABLE T_PERMISSION
(
    id          bigint primary key auto_increment comment '自增主键',
    code        varchar(100) not null comment '权限标识',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_permission_code (code)
) COMMENT ='权限表';

insert into T_PERMISSION(id, code)
values (1, '*');
insert into T_PERMISSION(id, code)
values (2, 'user.select');
insert into T_PERMISSION(id, code)
values (3, 'user.insert');
insert into T_PERMISSION(id, code)
values (4, 'user.update');
insert into T_PERMISSION(id, code)
values (5, 'user.delete');


create table T_ROLE_PERMISSION
(
    id            bigint primary key auto_increment comment '自增主键',
    role_id       bigint not null comment '角色id',
    permission_id bigint not null comment '权限id',
    create_user   varchar(30) default null comment '创建者',
    create_time   datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user   varchar(30) default null comment '修改者',
    update_time   datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_role_permission (role_id, permission_id)
);

insert into T_ROLE_PERMISSION(id, role_id, permission_id)
values (1, 1, 1);
insert into T_ROLE_PERMISSION(id, role_id, permission_id)
values (2, 2, 2);