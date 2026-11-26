package com.etl.api.ai.service.retriever;

import com.etl.api.ai.config.AiContentRetrievalProperties;
import com.etl.api.ai.service.manager.AiVectorStoreManager;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 知识库检索源：按调用上下文包装构建 {@link EmbeddingStoreContentRetriever}（RAG 管道的知识库源）。
 *
 * <p>EmbeddingStore / EmbeddingModel / 检索参数均为全局唯一配置（yaml 装配，应用启动即就绪），
 * 检索范围限定 bot 挂载的知识库：chat 入口解析 knowledgeIds 后经
 * {@link com.etl.api.ai.service.manager.AiContentRetrieverManager#getRetrievalAugmentor} 直传本类
 * {@link #getRetriever(List)}，经 metadata IsIn 过滤限定召回范围；未绑定知识库返回 null，
 * 上层跳过本源，不阻断聊天。
 *
 * <p>delegate 为轻量对象，每次检索按当前 knowledge_id 集合重建（store/model 均为共享实例），
 * bot 绑定知识库变更即时生效，无需缓存失效。
 *
 * @author chen
 * @since 2026-11-26
 */
@Component
@RequiredArgsConstructor
public class AiEmbeddingStoreContentRetriever implements AiContentRetriever {
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final AiContentRetrievalProperties retrievalProperties;

    @Override
    public String getName() {
        return "knowledge_base";
    }

    @Override
    public String getDescription() {
        return """
                检索该助手绑定的知识库文档内容（内部文档、制度、资料、数据说明等）。\
                用户提问涉及知识库内容或需要文档依据时选择本源。\
                """;
    }

    @Override
    public ContentRetriever getRetriever(List<Long> knowledgeIds) {
        if (CollectionUtils.isEmpty(knowledgeIds)) {
            return null;
        }

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(retrievalProperties.getMaxResults())
                .minScore(retrievalProperties.getMinScore())
                .filter(
                        MetadataFilterBuilder.metadataKey(AiVectorStoreManager.METADATA_KNOWLEDGE_ID)
                                .isIn(knowledgeIds.stream().map(String::valueOf).toList())
                )
                .build();
    }
}
