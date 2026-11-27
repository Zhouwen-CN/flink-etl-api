package com.etl.api.ai.service.retriever;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * 平台检索源抽象：声明检索源名称并提供 LC4j {@link ContentRetriever}
 * （"检索源提供一个检索器"，而非继承检索器本身），供 RAG 管道（AiRagManager）
 * 按源路由与 rag_source 来源标注使用，新增检索源实现本接口即可接入。
 *
 * @author chen
 * @since 2026-11-25
 */
public interface AiContentRetriever extends InitializingBean {

    /**
     * 描述，用来让大模型判断路由
     */
    String description();

    /**
     * 获取内容检索器
     * @param knowledgeIds 知识库id列表，如果参数越来越多，可以用类来装
     */
    ContentRetriever getRetriever(List<Long> knowledgeIds);

    /**
     * 默认什么都不做
     */
    @Override
    default void afterPropertiesSet(){}
}
