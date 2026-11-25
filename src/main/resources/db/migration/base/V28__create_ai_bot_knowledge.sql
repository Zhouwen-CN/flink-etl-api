create table T_AI_BOT_KNOWLEDGE
(
    id           bigint primary key auto_increment comment '自增主键',
    bot_id       bigint not null comment 'bot id',
    knowledge_id bigint not null comment '知识库id',
    create_user  varchar(30) default null comment '创建者',
    create_time  datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user  varchar(30) default null comment '修改者',
    update_time  datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_ai_bot_knowledge (bot_id, knowledge_id)
) comment 'Bot 知识库挂载表';
