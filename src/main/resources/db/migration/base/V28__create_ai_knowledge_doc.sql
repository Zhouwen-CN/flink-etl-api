create table T_AI_KNOWLEDGE_DOC
(
    id                bigint primary key auto_increment comment '自增主键',
    knowledge_id      bigint        not null comment '知识库id（T_AI_KNOWLEDGE）',
    file_name         varchar(255)  not null comment '原始文件名（兼作文档标题）',
    file_type         varchar(20)   not null comment '文件类型（pdf/docx/md/txt/xlsx）',
    file_content      longblob      not null comment '原始文件字节（blob入库，支持重解析）',
    content           longtext      default null comment '解析后的全文',
    parse_status      varchar(20)   not null default 'WAITING' comment '解析状态：WAITING/PROCESSING/SUCCESS/FAILED',
    parse_fail_reason varchar(1024) default null comment '解析失败原因',
    create_user       varchar(30)   default null comment '创建者',
    create_time       datetime      default CURRENT_TIMESTAMP comment '创建时间',
    update_user       varchar(30)   default null comment '修改者',
    update_time       datetime      default CURRENT_TIMESTAMP comment '更新时间',
    key idx_ai_knowledge_doc_knowledge (knowledge_id)
) comment 'AI 知识库文档表';

create table T_AI_KNOWLEDGE_DOC_CHUNK
(
    id          bigint primary key auto_increment comment '自增主键（同时作为向量库文档ID）',
    document_id bigint      not null comment '文档id（T_AI_KNOWLEDGE_DOC）',
    knowledge_id bigint     not null comment '知识库id（冗余，检索过滤用）',
    content     text        not null comment '分块内容',
    create_user varchar(30) default null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP comment '创建时间',
    update_user varchar(30) default null comment '修改者',
    update_time datetime    default CURRENT_TIMESTAMP comment '更新时间',
    key idx_ai_knowledge_doc_chunk_doc (document_id)
) comment 'AI 知识库文档分块表';
