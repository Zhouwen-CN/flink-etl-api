create table T_DEPT
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(100) not null comment '部门名称',
    sort        int          not null default 0 comment '显示排序',
    parent_id   bigint                default null comment '父部门id，根部门为 null',
    create_user varchar(30)           default null comment '创建者',
    create_time datetime              default current_timestamp comment '创建时间',
    update_user varchar(30)           default null comment '修改者',
    update_time datetime              default current_timestamp comment '更新时间',
    unique index uk_dept_name (name)
) comment '部门表';


-- 用户添加所属部门列
alter table T_USER
    add column dept_id bigint default null comment '所属部门id';

-- 角色添加 数据范围列
alter table T_ROLE
    add column data_scope tinyint default null comment '数据范围（1全部，2本部门及以下，3本部门）';


-- 4张需要做数据权限的表，添加所属部门列
alter table T_ETL_PROJECT
    add column dept_id bigint default null comment '归属部门id';
alter table T_ETL_JOB
    add column dept_id bigint default null comment '归属部门id';
alter table T_ETL_JOB_INSTANCE
    add column dept_id bigint default null comment '归属部门id';
alter table T_SCHEDULE_JOB
    add column dept_id bigint default null comment '归属部门id';


-- 初始化角色数据范围：admin 角色（id=1）设为全部（data_scope=1），保证管理员全量可见
update T_ROLE
set data_scope = 1
where id = 1;


-- 添加数据范围字典
insert into T_DICT_TYPE(id, name, remark, status)
values (3, 'data_scope', '数据范围', 1);
insert into T_DICT_DATA(id, type_id, label, `value`, sort_id)
values (6, 3, '全部', 1, 1);
insert into T_DICT_DATA(id, type_id, label, `value`, sort_id)
values (7, 3, '本部门及以下', 2, 2);
insert into T_DICT_DATA(id, type_id, label, `value`, sort_id)
values (8, 3, '本部门', 3, 3);

-- 添加部门根节点
insert into T_DEPT(id, name)
values (1, '海勃数科')