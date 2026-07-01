create table if not exists T_FLINK_CHECKPOINT
(
    id           bigint primary key auto_increment comment '自增主键',
    job_id       varchar(50)  not null comment 'flink任务id',
    chk_id       bigint       not null comment '检查点 id',
    type         bit(1)       not null comment '类型(0检查点 1保存点)',
    path         varchar(255) not null comment '检查点路径',
    trigger_time datetime     not null comment '检查点触发时间',
    unique index uk_checkpoint_job_chk_id (job_id, chk_id)
) comment 'Flink检查点表';