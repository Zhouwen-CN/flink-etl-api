package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.entity.AiKnowledgeDocChunk;
import com.etl.api.ai.domain.form.AiKnowledgeDocChunkUpdateForm;
import com.etl.api.ai.domain.vo.AiKnowledgeDocChunkVO;
import com.etl.api.ai.mapper.AiKnowledgeDocChunkMapper;
import com.etl.api.ai.service.AiKnowledgeDocChunkService;
import com.etl.api.ai.service.manager.AiVectorStoreManager;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AI 知识库文档分块表 服务层实现。
 *
 * <p>向量库读写统一经 {@link AiVectorStoreManager}：LangChain4j EmbeddingStore 无统一 update 语义，
 * 编辑用 upsert（remove+addAll）重写同一 vectorId；向量操作失败抛 AiVectorDbConfigException，
 * 由 AiExceptionHandler 统一转错误响应。
 *
 * @author chen
 * @since 2026-11-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeDocChunkServiceImpl extends ServiceImpl<AiKnowledgeDocChunkMapper, AiKnowledgeDocChunk>
        implements AiKnowledgeDocChunkService {

    private final AiVectorStoreManager vectorStoreManager;

    @Override
    public PageVO<AiKnowledgeDocChunkVO> getPage(Long documentId, Integer currentPage, Integer pageSize, String keyword) {
        val page = this.queryChain()
                .eq(AiKnowledgeDocChunk::getDocumentId, documentId)
                .like(AiKnowledgeDocChunk::getContent, keyword, StringUtils.hasText(keyword))
                .orderBy(AiKnowledgeDocChunk::getId, true)
                .pageAs(Page.of(currentPage, pageSize), AiKnowledgeDocChunkVO.class);
        return PageVO.from(page);
    }

    @Override
    public ResponseVO<Void> updateChunkContent(AiKnowledgeDocChunkUpdateForm form) {
        val chunk = this.getById(form.getId());
        if (chunk == null) {
            return ResponseVO.recordNotFoundError("ChunkId", form.getId());
        }

        // 1. 改 DB
        val update = AiKnowledgeDocChunk.builder()
                .id(chunk.getId())
                .content(form.getContent())
                .build();
        this.updateById(update);

        // 2. 重新向量化并用同一 vectorId 写回向量库（upsert 保持 id 与 metadata 稳定）
        vectorStoreManager.upsertChunk(chunk, form.getContent());
        log.info("分块已更新并重新向量化, chunkId: {}, documentId: {}", chunk.getId(), chunk.getDocumentId());
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeChunk(Long chunkId) {
        val chunk = this.getById(chunkId);
        if (chunk == null) {
            return ResponseVO.recordNotFoundError("ChunkId", chunkId);
        }
        // 先删向量库再删 DB
        vectorStoreManager.removeChunk(chunk.getKnowledgeId(), chunk.getDocumentId(), chunkId);

        this.removeById(chunkId);
        log.info("删除分块, chunkId: {}, documentId: {}", chunkId, chunk.getDocumentId());
        return ResponseVO.ok();
    }
}
