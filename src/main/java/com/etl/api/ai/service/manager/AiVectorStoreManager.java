package com.etl.api.ai.service.manager;

import com.etl.api.ai.config.AiChatModelFactory;
import com.etl.api.ai.config.AiVectorStoreFactory;
import com.etl.api.ai.domain.entity.AiKnowledge;
import com.etl.api.ai.domain.entity.AiKnowledgeDocChunk;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.entity.AiVectorDatabase;
import com.etl.api.ai.exception.AiVectorDbConfigException;
import com.etl.api.ai.mapper.AiKnowledgeDocChunkMapper;
import com.etl.api.ai.mapper.AiKnowledgeMapper;
import com.etl.api.ai.mapper.AiModelMapper;
import com.etl.api.ai.mapper.AiVectorDatabaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 向量库操作管理器：统一收口知识库向量库的读写。
 *
 * <p>业务服务不直接持有 EmbeddingStore，store/model 解析、metadata 构建、分块向量 upsert/删除
 * 均经此类调用。vectorId 格式 doc_{documentId}:chk_{chunkId}（{@link #getVectorId} 统一生成），
 * segment metadata 携带 knowledge_id / document_id（Redis store 需显式声明 metadataKeys，见 AiVectorStoreFactory）。
 *
 * <p>LangChain4j EmbeddingStore 无统一 update 语义，upsert 用 remove + addAll(ids) 组合模拟。
 *
 * @author chen
 * @since 2026-11-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiVectorStoreManager {

    /**
     * segment metadata key（向量库持久化 + 检索结果来源标注用）
     */
    public static final String METADATA_KNOWLEDGE_ID = "knowledge_id";
    public static final String METADATA_DOCUMENT_ID = "document_id";
    public static final String METADATA_DOCUMENT_NAME = "document_name";

    /**
     * metadataKeys（构造 Redis store 时显式传入）
     */
    public static final List<String> METADATA_KEYS =
            List.of(METADATA_KNOWLEDGE_ID, METADATA_DOCUMENT_ID, METADATA_DOCUMENT_NAME);

    private final AiKnowledgeMapper knowledgeMapper;
    private final AiVectorDatabaseMapper vectorDatabaseMapper;
    private final AiModelMapper modelMapper;
    private final AiChatModelFactory chatModelFactory;
    private final AiVectorStoreFactory vectorStoreFactory;
    private final AiKnowledgeDocChunkMapper chunkMapper;

    /**
     * 知识库分块向量 ID：doc_{documentId}:chk_{chunkId}（向量 ID 必须能反查 DB 记录，禁止外部手工拼接）
     */
    private static String getVectorId(Long documentId, Long chunkId) {
        return "doc_" + documentId + ":chk_" + chunkId;
    }

    /**
     * 获取知识库对应的 EmbeddingStore（复用向量库工厂两层缓存）
     */
    public EmbeddingStore<TextSegment> getEmbeddingStore(Long knowledgeId) {
        AiKnowledge knowledge = requireKnowledge(knowledgeId);
        AiVectorDatabase vectorDatabase = vectorDatabaseMapper.selectOneById(knowledge.getVectorDbId());
        if (vectorDatabase == null) {
            throw new AiVectorDbConfigException("向量数据库配置不存在: " + knowledge.getVectorDbId());
        }
        return vectorStoreFactory.getEmbeddingStore(knowledge, vectorDatabase, METADATA_KEYS);
    }

    /**
     * 获取知识库绑定的 EmbeddingModel
     */
    public EmbeddingModel getEmbeddingModel(Long knowledgeId) {
        AiKnowledge knowledge = requireKnowledge(knowledgeId);
        AiModel embedModel = modelMapper.selectOneById(knowledge.getEmbedModelId());
        if (embedModel == null) {
            throw new AiVectorDbConfigException("Embedding 模型不存在: " + knowledge.getEmbedModelId());
        }
        return chatModelFactory.getEmbeddingModel(embedModel);
    }

    /**
     * 构建知识库检索器：maxResults/minScore 取知识库级配置（recall_top_k / min_score 字段，
     * 即检索时生效的"最大召回条数/相似度下限"）。
     *
     * <p>store 集合按知识库独立派生（kb_{id}），检索天然只命中本库数据，无需 metadata 过滤；
     * retriever 为轻量对象，每次构建（store/model 均来自工厂缓存），召回参数改动即生效。
     */
    public EmbeddingStoreContentRetriever buildContentRetriever(Long knowledgeId) {
        AiKnowledge knowledge = requireKnowledge(knowledgeId);
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(getEmbeddingStore(knowledgeId))
                .embeddingModel(getEmbeddingModel(knowledgeId))
                .maxResults(knowledge.getRecallTopK())
                .minScore(knowledge.getMinScore())
                .build();
    }

    /**
     * 写入/更新单个分块向量（remove + addAll 幂等 upsert，内部解析 model/store）
     *
     * @param chunk    分块（须含 id/documentId/knowledgeId）
     * @param content  向量化内容（可与 chunk.content 不同，如编辑后的新内容）
     * @param fileName 来源文档名（随 metadata 固化，检索结果直接携带，免来源反查）
     */
    public void upsertChunk(AiKnowledgeDocChunk chunk, String content, String fileName) {
        String vectorId = getVectorId(chunk.getDocumentId(), chunk.getId());
        val metadata = new Metadata()
                .put(METADATA_KNOWLEDGE_ID, String.valueOf(chunk.getKnowledgeId()))
                .put(METADATA_DOCUMENT_ID, String.valueOf(chunk.getDocumentId()))
                .put(METADATA_DOCUMENT_NAME, fileName);
        val segment = TextSegment.from(content, metadata);
        Embedding embedding = getEmbeddingModel(chunk.getKnowledgeId()).embed(segment).content();
        EmbeddingStore<TextSegment> store = getEmbeddingStore(chunk.getKnowledgeId());
        store.remove(vectorId);
        store.addAll(List.of(vectorId), List.of(embedding), List.of(segment));
    }

    /**
     * 删除单个分块向量
     */
    public void removeChunk(Long knowledgeId, Long documentId, Long chunkId) {
        getEmbeddingStore(knowledgeId).remove(getVectorId(documentId, chunkId));
    }

    /**
     * 按分块 id 批量删除向量（单文档范围：vectorId 含 documentId，供失败清理与文档删除/重解析级联使用）
     */
    public void removeChunks(Long knowledgeId, Long documentId, Collection<Long> chunkIds) {
        if (chunkIds == null || chunkIds.isEmpty()) {
            return;
        }
        List<String> ids = chunkIds.stream()
                .map(chunkId -> getVectorId(documentId, chunkId))
                .toList();
        getEmbeddingStore(knowledgeId).removeAll(ids);
    }

    /**
     * 删除某文档在向量库中的全部向量与分块表数据（文档删除/重解析级联；不删文档本身）
     *
     * <p>先删向量库再删分块表：向量删除失败时 DB 分块仍在，可重试清理；反之若先删 DB 则向量成孤儿。
     */
    public void removeByDocument(Long knowledgeId, Long documentId) {
        List<Long> chunkIds = chunkMapper.selectListByQueryAs(
                QueryWrapper.create()
                        .select(AiKnowledgeDocChunk::getId)
                        .eq(AiKnowledgeDocChunk::getDocumentId, documentId),
                Long.class
        );

        removeChunks(knowledgeId, documentId, chunkIds);
        chunkMapper.deleteByQuery(
                QueryWrapper.create().eq(AiKnowledgeDocChunk::getDocumentId, documentId)
        );
    }

    private AiKnowledge requireKnowledge(Long knowledgeId) {
        AiKnowledge knowledge = knowledgeMapper.selectOneById(knowledgeId);
        if (knowledge == null) {
            throw new AiVectorDbConfigException("知识库不存在: " + knowledgeId);
        }
        return knowledge;
    }
}
