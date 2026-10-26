create table T_ETL_JOB_CONFIG_VERSION
(
    id             bigint primary key auto_increment comment '自增主键',
    job_id         bigint  not null comment '任务id',
    version        int     not null comment '版本号（任务内自增，从1开始）',
    config         text    not null comment 'json配置快照',
    operation_type tinyint not null default 1 comment '操作类型(1创建 2更新 3回滚)',
    source_version int              default null comment '回滚来源版本号',
    create_user    varchar(30)      default null comment '创建者',
    create_time    datetime         default CURRENT_TIMESTAMP comment '创建时间',
    update_user    varchar(30)      default null comment '修改者',
    update_time    datetime         default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_etl_job_config_ver (job_id, version)
) comment 'ETL任务配置版本表'
