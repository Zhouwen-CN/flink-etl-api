package com.etl.api.ai.service.manager;

import com.etl.api.ai.config.AiContentRetrievalProperties;
import com.etl.api.ai.domain.entity.AiKnowledgeDocChunk;
import com.etl.api.ai.mapper.AiKnowledgeDocChunkMapper;
import com.mybatisflex.core.query.QueryWrapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 向量库操作管理器：统一收口知识库向量库的读写。
 *
 * <p>EmbeddingStore / EmbeddingModel / 检索参数均为全局唯一配置（yaml 装配，直接注入），
 * 所有知识库数据写入同一集合；向量 ID 由 {@code embeddingStore.generateIds(int)} 生成并
 * 固化在分块表 vector_id 列（DB 是 chunk ↔ vector 映射的唯一事实来源），编辑/删除按其直接操作；
 * segment metadata 携带 knowledge_id / document_id / document_name，
 * 检索经 knowledge_id IsIn 过滤实现知识库范围隔离。
 *
 * <p>LangChain4j EmbeddingStore 无统一 update 语义，编辑用 remove + addAll(同一 vectorId) 模拟。
 *
 * @author chen
 * @since 2026-11-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiVectorStoreManager {

    /**
     * segment metadata key（向量库持久化 + 检索结果来源标注/知识库隔离过滤用）
     */
    public static final String METADATA_KNOWLEDGE_ID = "knowledge_id";
    public static final String METADATA_DOCUMENT_ID = "document_id";
    public static final String METADATA_DOCUMENT_NAME = "document_name";

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final AiContentRetrievalProperties retrievalProperties;
    private final AiKnowledgeDocChunkMapper chunkMapper;

    /**
     * 构建 segment metadata（knowledge_id 检索隔离 / document_id 来源 / document_name 检索结果直接携带）
     */
    private static Metadata buildMetadata(Long knowledgeId, Long documentId, String fileName) {
        return new Metadata()
                .put(METADATA_KNOWLEDGE_ID, String.valueOf(knowledgeId))
                .put(METADATA_DOCUMENT_ID, String.valueOf(documentId))
                .put(METADATA_DOCUMENT_NAME, fileName);
    }

    /**
     * 知识库隔离过滤条件：metadata.knowledge_id 在给定知识库 id 集合内（共享集合下限定召回范围）
     */
    private static Filter knowledgeIdFilter(Collection<Long> knowledgeIds) {
        return MetadataFilterBuilder.metadataKey(METADATA_KNOWLEDGE_ID)
                .isIn(knowledgeIds.stream().map(String::valueOf).toList());
    }

    /**
     * 构建知识库检索器：无论挂载多少知识库均返回一个 retriever，
     * 经 knowledge_id IsIn metadata 过滤限定召回范围；
     * maxResults/minScore 取全局检索配置（custom.ai.retrieval）。
     *
     * <p>retriever 为轻量对象，每次构建（store/model 均为共享实例），检索参数改动重启生效。
     */
    public EmbeddingStoreContentRetriever buildContentRetriever(Collection<Long> knowledgeIds) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(retrievalProperties.getMaxResults())
                .minScore(retrievalProperties.getMinScore())
                .filter(knowledgeIdFilter(knowledgeIds))
                .build();
    }

    /**
     * 批量写入文档分块向量（解析入库流水线用）：segments 携带 metadata，embedAll 批量向量化，
     * generateIds 生成向量 ID 后 addAll 一次写入；返回向量 ID 列表（调用方固化到分块表 vector_id 列）。
     *
     * @param contents   分块内容列表（顺序即向量 ID 顺序）
     * @param knowledgeId 知识库id（metadata）
     * @param documentId  文档id（metadata）
     * @param fileName    来源文档名（metadata）
     * @return 与 contents 一一对应的向量 ID 列表
     */
    public List<String> insertChunkVectors(List<String> contents, Long knowledgeId, Long documentId, String fileName) {
        val segments = contents.stream()
                .map(content -> TextSegment.from(content, buildMetadata(knowledgeId, documentId, fileName)))
                .toList();
        val embeddings = embeddingModel.embedAll(segments).content();
        val vectorIds = embeddingStore.generateIds(segments.size());
        embeddingStore.addAll(vectorIds, embeddings, segments);
        return vectorIds;
    }

    /**
     * 写入/更新单个分块向量（remove + addAll 同一 vectorId 模拟 upsert，id 与 metadata 保持稳定）
     *
     * @param chunk    分块（须含 vectorId/knowledgeId/documentId）
     * @param content  向量化内容（可与 chunk.content 不同，如编辑后的新内容）
     * @param fileName 来源文档名（随 metadata 固化，检索结果直接携带，免来源反查）
     */
    public void upsertChunk(AiKnowledgeDocChunk chunk, String content, String fileName) {
        val metadata = buildMetadata(chunk.getKnowledgeId(), chunk.getDocumentId(), fileName);
        val segment = TextSegment.from(content, metadata);
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.remove(chunk.getVectorId());
        embeddingStore.addAll(List.of(chunk.getVectorId()), List.of(embedding), List.of(segment));
    }

    /**
     * 删除单个分块向量
     */
    public void removeChunk(String vectorId) {
        embeddingStore.remove(vectorId);
    }

    /**
     * 按向量 ID 批量删除向量
     */
    public void removeChunks(Collection<String> vectorIds) {
        if (vectorIds == null || vectorIds.isEmpty()) {
            return;
        }
        embeddingStore.removeAll(vectorIds);
    }

    /**
     * 删除某文档在向量库中的全部向量与分块表数据（文档删除/重解析级联；不删文档本身）
     *
     * <p>先删向量库再删分块表：向量删除失败时 DB 分块仍在（vector_id 可重试清理）；反之若先删 DB 则向量成孤儿。
     */
    public void removeByDocument(Long documentId) {
        List<String> vectorIds = chunkMapper.selectListByQueryAs(
                QueryWrapper.create()
                        .select(AiKnowledgeDocChunk::getVectorId)
                        .eq(AiKnowledgeDocChunk::getDocumentId, documentId),
                String.class
        );

        removeChunks(vectorIds);
        chunkMapper.deleteByQuery(
                QueryWrapper.create().eq(AiKnowledgeDocChunk::getDocumentId, documentId)
        );
    }
}
