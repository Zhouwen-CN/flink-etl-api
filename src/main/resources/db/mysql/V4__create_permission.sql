CREATE TABLE T_PERMISSION
(
    id          bigint primary key auto_increment comment '自增主键',
    name varchar(30) not null comment '权限名称',
    code varchar(30) default null comment '权限标识符',
    type TINYINT     default 0 comment '类型(0按钮 1管理员)',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_permission_code (code)
) COMMENT ='权限表';

/**
  权限编码约定：routeName.operationType，由前端写入
  菜单、按钮权限，由前端自己控制
 */

-- admin
insert into T_PERMISSION(id, name, code, type)
values (1, '所有权限(管理员)', '*', 1);

-- user
insert into T_PERMISSION(id, name, code)
values (2, '查询', 'user.select');
insert into T_PERMISSION(id, name, code)
values (3, '新增', 'user.insert');
insert into T_PERMISSION(id, name, code)
values (4, '更新', 'user.update');
insert into T_PERMISSION(id, name, code)
values (5, '删除', 'user.delete');

-- role
insert into T_PERMISSION(id, name, code)
values (6, '查询', 'role.select');
insert into T_PERMISSION(id, name, code)
values (7, '新增', 'role.insert');
insert into T_PERMISSION(id, name, code)
values (8, '更新', 'role.update');
insert into T_PERMISSION(id, name, code)
values (9, '删除', 'role.delete');


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
insert into T_ROLE_PERMISSION(id, role_id, permission_id)
values (3, 2, 3);
insert into T_ROLE_PERMISSION(id, role_id, permission_id)
values (4, 2, 4);
insert into T_ROLE_PERMISSION(id, role_id, permission_id)
values (5, 2, 5);