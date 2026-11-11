create table T_AI_MODEL
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(64) not null comment '模型唯一标识',
    format      tinyint       default 1 comment '接口格式: 1-OpenAI Chat Completions, 2-OpenAI Responses, 3-Anthropic Messages',
    base_url    varchar(256)  default null comment '大模型请求地址',
    api_key     varchar(1000) default null comment '大模型 API KEY',
    model_name  varchar(64)   default null comment '模型名称，如 gpt-4o-mini',
    create_user varchar(30)   default null comment '创建者',
    create_time datetime      default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30)   default null comment '修改者',
    update_time datetime      default CURRENT_TIMESTAMP comment '更新时间',
    unique index uk_ai_model_name (name)
) comment 'AI模型配置表';
