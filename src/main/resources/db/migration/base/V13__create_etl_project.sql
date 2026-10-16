create table T_ETL_PROJECT
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(50) not null comment '项目名称',
    description varchar(100) default null comment '项目描述',
    create_user varchar(30)  default null comment '创建者',
    create_time datetime     default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30)  default null comment '修改者',
    update_time datetime     default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_etl_project_name (name)
) comment 'ETL项目表';