create table T_AI_KNOWLEDGE
(
    id                 bigint primary key auto_increment comment '自增主键',
    vector_db_id bigint      not null comment '向量数据库id（T_AI_VECTOR_DATABASE）',
    embed_model_id     bigint      not null comment 'Embedding模型id（T_AI_MODEL，type=2）',
    name               varchar(50) not null comment '知识库名称',
    description        varchar(255)         default null comment '知识库描述',
    collection_name    varchar(64) not null comment '向量库集合名/索引名（服务端生成 kb_{id}）',
    dimension          int         not null comment '向量维度（创建时探测固化）',
    recall_top_k       int         not null default 5 comment '最大召回条数',
    min_score          double      not null default 0.20 comment '相似度下限（LangChain4j minScore 语义，0~1）',
    create_user        varchar(30)          default null comment '创建者',
    create_time        datetime             default CURRENT_TIMESTAMP comment '创建时间',
    update_user        varchar(30)          default null comment '修改者',
    update_time        datetime             default CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_ai_knowledge_name (name),
    unique key uk_ai_knowledge_collection (collection_name)
) comment 'AI 知识库表';
