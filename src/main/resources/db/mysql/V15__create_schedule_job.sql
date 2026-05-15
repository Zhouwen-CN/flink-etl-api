create table T_SCHEDULE_JOB
(
    id              bigint primary key auto_increment comment '自增主键',
    name            varchar(30) not null comment '任务名称',
    cron_expression varchar(30) not null comment 'cron 表达式',
    etl_job_id      bigint      default null comment 'ETL任务ID',
    job_enable      bit(1)      default 0 comment '是否开启',
    create_user     varchar(30) default null comment '创建者',
    create_time     datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user     varchar(30) default null comment '修改者',
    update_time     datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_schedule_job_name (name)
) comment = '定时任务表';