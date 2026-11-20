package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiKnowledgeDocChunk;
import com.etl.api.ai.domain.form.AiKnowledgeDocChunkUpdateForm;
import com.etl.api.ai.domain.vo.AiKnowledgeDocChunkVO;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * AI 知识库文档分块表 服务层。
 *
 * <p>分块只由解析流水线产生，人工只做编辑/删除（对齐 AIFlowy）。
 *
 * @author chen
 * @since 2026-11-20
 */
public interface AiKnowledgeDocChunkService extends IService<AiKnowledgeDocChunk> {

    /**
     * 分块分页查看（按 id 升序，即文档顺序；keyword 可选，按内容模糊匹配）
     */
    PageVO<AiKnowledgeDocChunkVO> getPage(Long documentId, Integer currentPage, Integer pageSize, String keyword);

    /**
     * 编辑分块内容（改 DB 后用同一 chunk.id 同步重新向量化）
     */
    ResponseVO<Void> updateChunkContent(AiKnowledgeDocChunkUpdateForm form);

    /**
     * 删除分块（同步删向量库记录）
     */
    ResponseVO<Void> removeChunk(Long chunkId);
}
