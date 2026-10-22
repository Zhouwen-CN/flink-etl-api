create table if not exists T_ETL_FLINK_CHECKPOINT_SYNC
(
    id           bigint primary key auto_increment comment '自增主键',
    job_id   bigint not null comment '任务id',
    chk_id   int    not null comment '检查点 id',
    chk_size bigint not null comment '检查点大小',
    type         bit(1)       not null comment '类型(0检查点 1保存点)',
    path         varchar(255) not null comment '检查点路径',
    trigger_time datetime     not null comment '检查点触发时间',
    unique index uk_etl_flink_chk_sync (job_id, chk_id)
) comment 'Flink检查点同步表';