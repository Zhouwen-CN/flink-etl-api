create table T_AI_KNOWLEDGE
(
    id                 bigint primary key auto_increment comment '自增主键',
    name               varchar(50) not null comment '知识库名称',
    description        varchar(255)         default null comment '知识库描述',
    chunk_size   int    not null comment '单个分块的最大字符数（recursive 分块的 maxSegmentSize，LangChain4j 无 tokenizer 时按字符计）',
    overlap_size int    not null comment '相邻分块的重叠字符数（maxOverlapSize，须小于 chunk_size）',
    create_user        varchar(30)          default null comment '创建者',
    create_time        datetime             default CURRENT_TIMESTAMP comment '创建时间',
    update_user        varchar(30)          default null comment '修改者',
    update_time        datetime             default CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_ai_knowledge_name (name)
) comment 'AI 知识库表';
