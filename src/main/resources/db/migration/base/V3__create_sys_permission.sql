CREATE TABLE T_SYS_PERMISSION
(
    id          bigint primary key auto_increment comment '自增主键',
    name varchar(30) not null comment '权限名称',
    code varchar(30) default null comment '权限标识符',
    type TINYINT     default 0 comment '类型(0按钮 1管理员)',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_sys_permission_code (code)
) COMMENT ='权限表';

/**
  权限编码约定：routeName.operationType，由前端写入
  菜单、按钮权限，由前端自己控制
 */

create table T_SYS_ROLE_PERMISSION
(
    id            bigint primary key auto_increment comment '自增主键',
    role_id       bigint not null comment '角色id',
    permission_id bigint not null comment '权限id',
    create_user   varchar(30) default null comment '创建者',
    create_time   datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user   varchar(30) default null comment '修改者',
    update_time   datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_sys_role_permission (role_id, permission_id)
);