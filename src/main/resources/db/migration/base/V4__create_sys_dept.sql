create table T_SYS_DEPT
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

-- 添加部门根节点
insert into T_SYS_DEPT(id, name)
values (1, '海勃数科');


-- 4张需要做数据权限的表，添加所属部门列
alter table T_ETL_PROJECT
    add column dept_id bigint default null comment '归属部门id' after description;
alter table T_ETL_JOB
    add column dept_id bigint default null comment '归属部门id' after config;
alter table T_ETL_JOB_INSTANCE
    add column dept_id bigint default null comment '归属部门id' after duration;
alter table T_SCHEDULE_JOB
    add column dept_id bigint default null comment '归属部门id' after job_enable;