create table T_ETL_FLINK_JAR_SYNC
(
    id         bigint primary key auto_increment comment '自增主键',
    cluster_id bigint       not null comment '集群id',
    jar_id varchar(200) not null comment 'jar包id',
    jar_name   varchar(100) not null comment 'jar包名称',
    uploaded   bigint       not null comment '上传时间',
    unique index uk_etl_flink_jar_sync (cluster_id, jar_id)
) comment 'Flink Jar同步表';