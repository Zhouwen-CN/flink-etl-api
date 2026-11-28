package com.etl.api.ai.domain.dto;

import lombok.Data;

/**
 * RAG 引用资料条目（Cherry Studio 风格）：既序列化进提示词的参考资料 JSON 数组，
 * 也随 done 事件/toAttributes 落库供前端渲染引用卡片，两处同一份结构。
 *
 * <p>注入阶段按 segment metadata 判别形态（LC4j 1.23.0 源码核实）：
 * 知识库源携带 document_name（作为标题）；web 源仅携带 url，标题拼在检索文本首行。
 *
 * @author chen
 * @since 2026-11-26
 */
@Data
public class AiRetrievalContentDTO {

    /**
     * 编号（从 1 开始，与提示词 JSON 及正文 [n] 下标对应）
     */
    private Integer number;

    /**
     * 标题：知识库=文档名；web=网页标题（LC4j 将标题拼在检索文本首行）
     */
    private String title;

    /**
     * 引用内容
     */
    private String content;

    /**
     * 网页地址（仅 web 源）
     */
    private String url;

    /**
     * 向量数据库的 id（仅 知识库 源）
     */
    private String embeddingId;

    /**
     * 相关度分数
     */
    private Double score;
}
