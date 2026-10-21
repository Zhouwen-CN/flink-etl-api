create table T_SYS_DEPT
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(100) not null comment '部门名称',
    sort        int          not null default 0 comment '显示排序',
    parent_id   bigint                default null comment '父部门id，根部门为 null',
    parent_path varchar(200) default null comment '物化路径：根到自身的 id 链',
    create_user varchar(30)           default null comment '创建者',
    create_time datetime              default current_timestamp comment '创建时间',
    update_user varchar(30)           default null comment '修改者',
    update_time datetime              default current_timestamp comment '更新时间',
    unique index uk_sys_dept_name (name)
) comment '部门表';

-- 添加部门根节点
insert into T_SYS_DEPT(id, name, parent_path)
values (1, '海勃数科', '/1/');