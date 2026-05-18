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
values (2, '查询用户', 'user.select');
insert into T_PERMISSION(id, name, code)
values (3, '新增用户', 'user.insert');
insert into T_PERMISSION(id, name, code)
values (4, '更新用户', 'user.update');
insert into T_PERMISSION(id, name, code)
values (5, '删除用户', 'user.delete');

-- role
insert into T_PERMISSION(id, name, code)
values (6, '查询角色', 'role.select');
insert into T_PERMISSION(id, name, code)
values (7, '新增角色', 'role.insert');
insert into T_PERMISSION(id, name, code)
values (8, '更新角色', 'role.update');
insert into T_PERMISSION(id, name, code)
values (9, '删除角色', 'role.delete');

-- permission
insert into T_PERMISSION(id, name, code)
values (10, '查询权限', 'permission.select');
insert into T_PERMISSION(id, name, code)
values (11, '新增权限', 'permission.insert');
insert into T_PERMISSION(id, name, code)
values (12, '更新权限', 'permission.update');
insert into T_PERMISSION(id, name, code)
values (13, '删除权限', 'permission.delete');

-- dict
insert into T_PERMISSION(id, name, code)
values (14, '查询字典', 'dict.select');
insert into T_PERMISSION(id, name, code)
values (15, '新增字典', 'dict.insert');
insert into T_PERMISSION(id, name, code)
values (16, '更新字典', 'dict.update');
insert into T_PERMISSION(id, name, code)
values (17, '删除字典', 'dict.delete');

-- schedule
insert into T_PERMISSION(id, name, code)
values (18, '查询定时', 'schedule.select');
insert into T_PERMISSION(id, name, code)
values (19, '新增定时', 'schedule.insert');
insert into T_PERMISSION(id, name, code)
values (20, '更新定时', 'schedule.update');
insert into T_PERMISSION(id, name, code)
values (21, '删除定时', 'schedule.delete');

-- cluster
insert into T_PERMISSION(id, name, code)
values (22, '查询集群', 'cluster.select');
insert into T_PERMISSION(id, name, code)
values (23, '新增集群', 'cluster.insert');
insert into T_PERMISSION(id, name, code)
values (24, '更新集群', 'cluster.update');
insert into T_PERMISSION(id, name, code)
values (25, '删除集群', 'cluster.delete');

-- jar
insert into T_PERMISSION(id, name, code)
values (26, '查询jar', 'jar.select');
insert into T_PERMISSION(id, name, code)
values (27, '新增jar', 'jar.insert');
insert into T_PERMISSION(id, name, code)
values (28, '更新jar', 'jar.update');
insert into T_PERMISSION(id, name, code)
values (29, '删除jar', 'jar.delete');

-- job
insert into T_PERMISSION(id, name, code)
values (30, '查询任务', 'job.select');
insert into T_PERMISSION(id, name, code)
values (31, '新增任务', 'job.insert');
insert into T_PERMISSION(id, name, code)
values (32, '更新任务', 'job.update');
insert into T_PERMISSION(id, name, code)
values (33, '删除任务', 'job.delete');

-- instance
insert into T_PERMISSION(id, name, code)
values (34, '查询实例', 'instance.select');
insert into T_PERMISSION(id, name, code)
values (35, '新增实例', 'instance.insert');
insert into T_PERMISSION(id, name, code)
values (36, '更新实例', 'instance.update');
insert into T_PERMISSION(id, name, code)
values (37, '删除实例', 'instance.delete');

-- log
insert into T_PERMISSION(id, name, code)
values (38, '查询登入日志', 'login-log.select');
insert into T_PERMISSION(id, name, code)
values (39, '查询错误日志', 'error-log.select');

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