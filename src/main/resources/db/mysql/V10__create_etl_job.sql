create table T_ETL_JOB
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(30) not null comment '任务名称',
    cluster_id  bigint      not null comment 'flink集群id',
    jar_id      bigint      not null comment 'jar包id',
    config      text        not null comment 'json配置',
    type        tinyint     not null comment '任务类型(1batch 2streaming)',
    status      varchar(20) default null comment '任务状态',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_etl_job_name (name)
) comment 'ETL任务表'