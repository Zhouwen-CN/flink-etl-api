create table T_DICT_TYPE
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(30) not null comment '字典名称',
    remark      varchar(30) not null comment '字典描述',
    status      bit(1)      default 0 comment '字典是否启用：0-禁用，1-启用',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_dict_type_name (name)
) comment '字典类型表';

insert into T_DICT_TYPE(id, name, remark, status)
values (1, 'gender', '性别', 1);
insert into T_DICT_TYPE(id, name, remark, status)
values (2, 'job_type', '任务类型', 1);

create table T_DICT_DATA
(
    id          bigint primary key auto_increment comment '自增主键',
    type_id     bigint      not null comment '字典类型id',
    label       varchar(30) not null comment '字典键',
    `value`     bigint      not null comment '字典值',
    sort_id     int         default 0 comment '字典排序',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_dict_type_label (type_id, label)
) comment '字典数据表';

insert into T_DICT_DATA(id, type_id, label, `value`, sort_id)
values (1, 1, '未知', 0, 0);
insert into T_DICT_DATA(id, type_id, label, `value`, sort_id)
values (2, 1, '男', 1, 1);
insert into T_DICT_DATA(id, type_id, label, `value`, sort_id)
values (3, 1, '女', 2, 2);

insert into T_DICT_DATA(id, type_id, label, `value`, sort_id)
values (4, 2, 'BATCH', 1, 1);
insert into T_DICT_DATA(id, type_id, label, `value`, sort_id)
values (5, 2, 'STREAMING', 2, 2);