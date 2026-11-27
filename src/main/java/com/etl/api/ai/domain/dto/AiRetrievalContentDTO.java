package com.etl.api.ai.domain.dto;

import lombok.Data;

/**
 * RAG 引用来源条目（assistant 消息 attributes.getRetrievalContentList 的元素，仅前端展示，不发给大模型）。
 *
 * @author chen
 * @since 2026-11-26
 */
@Data
public class AiRetrievalContentDTO {

    /**
     * 来源：knowledge_base=知识库 / web_search=联网搜索
     */
    private String source;

    /**
     * 标题：知识库=文档名；web=网页标题（LC4j 将标题拼在检索文本首行）
     */
    private String title;

    /**
     * 网页地址（仅 web 源）
     */
    private String url;

    /**
     * 引用内容摘要（截断 200 字）
     */
    private String snippet;

    /**
     * 相似度分数（仅知识库源，保留 4 位小数）
     */
    private Double score;
}
