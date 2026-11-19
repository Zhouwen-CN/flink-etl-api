create table T_AI_VECTOR_DATABASE
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(50) not null comment '向量数据库名称',
    type        tinyint     not null comment '向量数据库类型（1-redis 2-milvus）',
    description varchar(255) default null comment '向量数据库描述',
    config_json text        not null comment '连接配置JSON（按type结构不同）',
    create_user varchar(30)  default null comment '创建者',
    create_time datetime     default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30)  default null comment '修改者',
    update_time datetime     default CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_ai_vector_database_name (name)
) comment 'AI 向量数据库表';
