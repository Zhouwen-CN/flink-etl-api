CREATE TABLE T_PERMISSION
(
    id          bigint primary key auto_increment comment '自增主键',
    name      varchar(50) not null comment '权限名称',
    code      varchar(30) default null comment '权限标识符',
    type      TINYINT     not null comment '类型(1菜单 2按钮)',
    parent_id bigint      default 0 comment '父级ID',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_permission_code (code)
) COMMENT ='权限表';

/**
  1.sa-token 只获取按钮权限
  2.菜单权限是给前端用的，比如动态加载菜单、权限编辑
 */
insert into T_PERMISSION(id, name, code, type, parent_id)
values (1, '所有权限(管理员)', '*', 2, 0);

insert into T_PERMISSION(id, name, code, type, parent_id)
values (2, '用户菜单', null, 1, 0);

insert into T_PERMISSION(id, name, code, type, parent_id)
values (3, '查询', 'user.select', 2, 2);
insert into T_PERMISSION(id, name, code, type, parent_id)
values (4, '新增', 'user.insert', 2, 2);
insert into T_PERMISSION(id, name, code, type, parent_id)
values (5, '更新', 'user.update', 2, 2);
insert into T_PERMISSION(id, name, code, type, parent_id)
values (6, '删除', 'user.delete', 2, 2);


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
insert into T_ROLE_PERMISSION(id, role_id, permission_id)
values (6, 2, 6);