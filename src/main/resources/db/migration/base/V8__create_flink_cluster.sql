create table T_FLINK_CLUSTER
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(30) not null comment '集群名称',
    job_manager_url varchar(100) not null comment '集群地址',
    version     varchar(30) default null comment 'Flink 版本',
    status      bit(1)      default 0 comment '集群状态(1启用 0停用)',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_flink_cluster_name (name)
) comment 'Flink 集群表';
