alter table t_etl_job
    add column description varchar(100) default null comment '任务描述' after name;

alter table t_cluster_uploaded_jar
    modify column jar_id varchar(200) not null comment 'jar包id';