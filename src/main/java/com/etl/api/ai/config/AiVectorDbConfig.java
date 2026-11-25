package com.etl.api.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 向量库装配配置（与 ChatModelFactory 并列）。
 *
 * <p>按 {@link AiVectorDbProperties#getType()} 条件装配全局唯一 {@link EmbeddingStore} Bean，
 * 应用启动即就绪，需要向量库读写的地方直接注入即可：
 * <ul>
 *   <li>MILVUS：{@link MilvusEmbeddingStore}（SDK 内部按 yaml 配置自建连接）；</li>
 *   <li>MEMORY：{@link InMemoryEmbeddingStore}（内置内存，开发/测试用）。</li>
 * </ul>
 *
 * <p>所有知识库数据写入同一集合，向量 ID 由 embeddingStore.generateIds 生成并固化在分块表
 * vector_id 列（编辑/删除按其操作）；
 * 检索隔离由 segment metadata 的 knowledge_id 过滤实现（见 AiVectorStoreManager）。
 *
 * @author chen
 * @since 2026-11-18
 */
@Configuration
@RequiredArgsConstructor
public class AiVectorDbConfig {

    private final AiVectorDbProperties vectorDbProperties;

    /**
     * 内存向量库单例（type=memory 时装配，开发/测试用）
     */
    @Bean
    @ConditionalOnProperty(prefix = "custom.ai.vector-db", name = "type", havingValue = "memory")
    public EmbeddingStore<TextSegment> inMemoryEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * Milvus 向量库单例（type=milvus 时装配）。
     * 集合在首次使用时按 yaml 固化的 collectionName/dimension 自动创建，应用关闭由 SDK 进程退出兜底。
     */
    @Bean
    @ConditionalOnProperty(prefix = "custom.ai.vector-db", name = "type", havingValue = "milvus")
    public EmbeddingStore<TextSegment> milvusEmbeddingStore() {
        val config = vectorDbProperties.getMilvus();
        val builder = MilvusEmbeddingStore.builder()
                .host(config.getHost())
                .port(config.getPort())
                .collectionName(config.getCollectionName())
                .dimension(config.getDimension());
        if (StringUtils.hasText(config.getUsername()) && StringUtils.hasText(config.getPassword())) {
            builder.username(config.getUsername());
            builder.password(config.getPassword());
        }
        if (StringUtils.hasText(config.getDatabaseName())) {
            builder.databaseName(config.getDatabaseName());
        }
        return builder.build();
    }
}
