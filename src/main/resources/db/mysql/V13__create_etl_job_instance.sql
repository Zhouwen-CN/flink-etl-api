create table T_ETL_JOB_INSTANCE
(
    id          varchar(50) primary key comment 'flink任务id',
    cluster_id  bigint  not null comment '集群id',
    jar_id      bigint  not null comment 'jar包id',
    job_id      bigint  not null comment '任务id',
    job_type    tinyint not null comment '任务类型(1batch 2streaming)',
    status      varchar(20) default null comment '任务状态',
    start_time  datetime    default null comment '任务开始时间',
    end_time    datetime    default null comment '任务结束时间',
    duration    bigint      default null comment '持续时间',
    create_user varchar(30) default null comment '创建着'
) comment 'ETL任务实例表'