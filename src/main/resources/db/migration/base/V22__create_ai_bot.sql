create table T_AI_BOT
(
    id             bigint primary key auto_increment comment '自增主键',
    model_id       bigint      not null comment '模型ID',
    name           varchar(50) not null comment 'bot名称',
    temperature    double      default null comment '模型温度(0-2之间)',
    top_p          double      default null comment 'Top-P(0-1之间)',
    system_prompt  text        default null comment '系统提示词',
    max_tool_calls int not null comment '单次对话最大工具调用轮数',
    status         bit(1)      default 1 comment '状态: 1-启用 0-停用',
    dept_id        bigint      not null comment '归属部门id',
    create_user    varchar(30) default null comment '创建者',
    create_time    datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user    varchar(30) default null comment '修改者',
    update_time    datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_ai_bot_name (name)
) comment 'AI Bot表';