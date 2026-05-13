create table T_CLUSTER_UPLOADED_JAR_SYNC
(
    id         bigint primary key auto_increment comment '自增主键',
    cluster_id bigint       not null comment '集群id',
    jar_id     varchar(100) not null comment 'jar包id',
    jar_name   varchar(100) not null comment 'jar包名称',
    uploaded   bigint       not null comment '上传时间',
    unique index uk_cluster_jar (cluster_id, jar_id)
) comment '集群已上传jar包同步表';