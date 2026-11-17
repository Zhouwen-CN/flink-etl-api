create table T_AI_MCP_SERVER
(
    id          bigint primary key auto_increment comment '自增主键',
    name        varchar(50) not null comment 'mcp服务名称',
    description varchar(255) default null comment 'mcp服务描述',
    config_json text        not null comment 'mcp连接配置（单个服务）',
    create_user varchar(30)  default null comment '创建者',
    create_time datetime     default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30)  default null comment '修改者',
    update_time datetime     default CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_ai_mcp_server_name (name)
) comment 'AI MCP服务表';

create table T_AI_MCP_TOOL
(
    id              bigint primary key auto_increment comment '自增主键',
    server_id       bigint       not null comment 'mcp服务id',
    name            varchar(255) not null comment '工具名',
    description     text        default null comment '工具描述（同步自MCP服务端）',
    input_schema    text        default null comment '工具参数Schema JSON（同步自MCP服务端）',
    output_template text        default null comment '自定义输出模板，工具结果回传模型前按模板包装（该工具所有挂载Bot共用）',
    create_user     varchar(30) default null comment '创建者',
    create_time     datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user     varchar(30) default null comment '修改者',
    update_time     datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_ai_mcp_server_tool (server_id, name)
) comment 'AI MCP工具表（服务端工具清单缓存，upsert同步）';

create table T_AI_BOT_MCP_TOOL
(
    id          bigint primary key auto_increment comment '自增主键',
    bot_id      bigint not null comment 'bot id',
    tool_id     bigint not null comment 'mcp工具id',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_ai_bot_mcp_tool (bot_id, tool_id)
) comment 'Bot MCP工具挂载表';
