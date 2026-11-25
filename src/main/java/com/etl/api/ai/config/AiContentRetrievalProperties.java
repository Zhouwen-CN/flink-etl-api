package com.etl.api.ai.config;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 知识库检索参数配置（全局唯一，替代原知识库级 recall_top_k / min_score 字段）。
 *
 * <p>Embedding 模型与向量集合均已全局化（yaml 装配），检索参数随之收敛为全局配置，
 * 每次检索经 knowledge_id metadata IsIn 过滤限定 bot 挂载的知识库范围。
 *
 * @author chen
 * @since 2026-11-25
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "custom.ai.retrieval")
public class AiContentRetrievalProperties implements InitializingBean {

    /**
     * 最大召回条数（LangChain4j EmbeddingStoreContentRetriever maxResults 语义）
     */
    private Integer maxResults = 5;

    /**
     * 相似度下限（LangChain4j minScore 语义，0~1）
     */
    private Double minScore = 0.6;

    @Override
    public void afterPropertiesSet() {
        if (maxResults == null || maxResults < 1) {
            throw new IllegalArgumentException("检索配置 maxResults 必须为正整数（custom.ai.retrieval.max-results）");
        }
        if (minScore == null || minScore < 0.0 || minScore > 1.0) {
            throw new IllegalArgumentException("检索配置 minScore 必须为 0~1 的小数（custom.ai.retrieval.min-score）");
        }
    }
}
