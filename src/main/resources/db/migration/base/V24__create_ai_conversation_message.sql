create table T_AI_CONVERSATION_MESSAGE
(
    id              bigint primary key auto_increment comment '自增主键',
    conversation_id bigint   not null comment '会话id',
    message         text     not null comment '会话消息（JSON）',
    created_time    datetime not null default current_timestamp comment '创建时间',
    index idx_ai_conversation_message (conversation_id)
) comment 'AI会话历史';