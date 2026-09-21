alter table t_etl_job
    add column description varchar(100) default null comment '任务描述' after name;