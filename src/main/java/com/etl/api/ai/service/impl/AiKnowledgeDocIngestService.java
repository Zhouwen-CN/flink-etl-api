package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.entity.AiKnowledge;
import com.etl.api.ai.domain.entity.AiKnowledgeDoc;
import com.etl.api.ai.domain.entity.AiKnowledgeDocChunk;
import com.etl.api.ai.enumeration.AiDocParseStatusEnum;
import com.etl.api.ai.mapper.AiKnowledgeDocChunkMapper;
import com.etl.api.ai.mapper.AiKnowledgeDocMapper;
import com.etl.api.ai.service.manager.AiVectorStoreManager;
import com.etl.api.sys.aop.IgnoreTransaction;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库文档向量化入库服务（解析 → 分块 → 落库 → 向量化 → 写向量库）。
 *
 * <p>异步流水线（ai 请求异步线程池 applicationTaskExecutor）：任何一步失败 → parse_status=FAILED + parse_fail_reason，
 * 并尽力清理已写入的分块/向量。向量库读写统一经 {@link AiVectorStoreManager}，
 * 向量 ID 由 embeddingStore.generateIds 生成并固化到分块表 vector_id 列。
 *
 * <p>分块参数（chunk_size/overlap_size）取知识库级配置；LangChain4j 1.23.0-beta33 的
 * DocumentSplitters.recursive(int,int) 无 tokenizer 重载，按字符计大小。
 *
 * @author chen
 * @since 2026-11-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeDocIngestService {

    /**
     * parse_fail_reason 列长度
     */
    private static final int MAX_FAIL_REASON_LENGTH = 1024;

    /**
     * Apache Tika 统一解析 pdf/docx/xlsx/md/txt（自动探测内容类型，无需按 file_type 分发解析器）
     */
    private final ApacheTikaDocumentParser tikaParser = new ApacheTikaDocumentParser();

    /**
     * 分块批量插入（无需回填自增 id，向量 ID 来自 embeddingStore.generateIds），保留 mapper
     */
    private final AiKnowledgeDocChunkMapper chunkMapper;
    private final AiVectorStoreManager vectorStoreManager;
    private final AiKnowledgeDocMapper knowledgeDocMapper;

    /**
     * ai 请求异步线程池（AiThreadPoolConfig.applicationTaskExecutor，bean 名与字段名一致按名称注入）
     */
    private final ThreadPoolTaskExecutor applicationTaskExecutor;

    private static String truncate(String message) {
        if (message == null) {
            return "未知错误";
        }
        return message.length() <= MAX_FAIL_REASON_LENGTH ? message : message.substring(0, MAX_FAIL_REASON_LENGTH);
    }

    /**
     * 提交异步解析（状态机 WAITING → PROCESSING → SUCCESS/FAILED，前端轮询 parse_status）
     */
    @IgnoreTransaction
    public void submitParse(Long documentId) {
        applicationTaskExecutor.execute(() -> parseAndVectorize(documentId));
    }

    /**
     * 异步解析流水线：读 blob → Tika 提取全文 → recursive 分块 → generateIds 生成向量 ID →
     * 批量写向量库 → 批量落分块表（含 vector_id）→ 更新 parse_status=SUCCESS + content。
     */
    private void parseAndVectorize(Long documentId) {
        // selectOneById 为 SELECT *，会带出 file_content 大字段（解析必需）
        AiKnowledgeDoc knowledgeDoc = knowledgeDocMapper.selectOneById(documentId);
        if (knowledgeDoc == null) {
            log.warn("解析任务对应文档不存在, documentId: {}", documentId);
            return;
        }

        AiKnowledge knowledge = QueryChain.of(AiKnowledge.class)
                .eq(AiKnowledge::getId, knowledgeDoc.getKnowledgeId())
                .one();
        if (knowledge == null) {
            updateStatus(documentId, AiDocParseStatusEnum.FAILED, "知识库不存在: " + knowledgeDoc.getKnowledgeId());
            return;
        }

        // 向量 ID 在写入向量库前生成，失败清理按其补偿删除（分块表按 documentId 兜底清理）
        List<String> insertedVectorIds = null;
        try {
            updateStatus(knowledgeDoc.getId(), AiDocParseStatusEnum.PROCESSING, null);

            // 1. 解析全文
            String text = parse(knowledgeDoc.getFileContent(), knowledgeDoc.getFileType());
            if (text == null || text.isBlank()) {
                throw new IllegalStateException("文档解析结果为空，未产生任何文本");
            }

            // 2. recursive 分块（按字符计，chunk_size/overlap_size 取知识库配置）
            List<TextSegment> splitSegments = DocumentSplitters.recursive(knowledge.getChunkSize(), knowledge.getOverlapSize()).split(Document.from(text));
            if (splitSegments.isEmpty()) {
                throw new IllegalStateException("分块结果为空，请检查文档内容与分块参数");
            }

            // 3. 批量向量入库：generateIds 生成向量 ID → embedAll 批量向量化 → addAll 一次写入
            val contents = splitSegments.stream().map(TextSegment::text).toList();
            insertedVectorIds = vectorStoreManager.insertChunkVectors(
                    contents, knowledgeDoc.getKnowledgeId(), knowledgeDoc.getId(), knowledgeDoc.getFileName());

            // 4. 批量落分块表（vector_id 一一对应固化）
            val chunks = new ArrayList<AiKnowledgeDocChunk>();
            for (int i = 0; i < contents.size(); i++) {
                chunks.add(AiKnowledgeDocChunk.builder()
                        .vectorId(insertedVectorIds.get(i))
                        .documentId(knowledgeDoc.getId())
                        .knowledgeId(knowledgeDoc.getKnowledgeId())
                        .content(contents.get(i))
                        .build());
            }
            chunkMapper.insertBatch(chunks);

            // 5. 成功：固化全文 + SUCCESS
            UpdateChain.of(AiKnowledgeDoc.class)
                    .set(AiKnowledgeDoc::getContent, text)
                    .set(AiKnowledgeDoc::getParseStatus, AiDocParseStatusEnum.SUCCESS.getCode())
                    .eq(AiKnowledgeDoc::getId, knowledgeDoc.getId())
                    .update();
            log.info("文档解析向量化完成, documentId: {}, chunks: {}", knowledgeDoc.getId(), contents.size());
        } catch (Exception e) {
            log.error("文档解析向量化失败, documentId: {}", knowledgeDoc.getId(), e);
            cleanupPartialData(knowledgeDoc.getId(), insertedVectorIds);
            updateStatus(knowledgeDoc.getId(), AiDocParseStatusEnum.FAILED, truncate(e.getMessage()));
        }
    }

    /**
     * 解析文件字节为全文文本（Apache Tika）
     *
     * @param fileContent 原始文件字节
     * @param fileType    扩展名（已过内容探测白名单校验，仅用于日志）
     * @return 解析后的全文
     * @throws IOException Tika 解析失败
     */
    private String parse(byte[] fileContent, String fileType) throws IOException {
        Document document = tikaParser.parse(new ByteArrayInputStream(fileContent));
        String text = document.text();
        log.info("文档解析完成, fileType: {}, 文件字节: {}, 全文字符: {}", fileType, fileContent.length,
                text == null ? 0 : text.length());
        return text;
    }

    /**
     * 失败清理：按向量 ID 尽力删除已写入的向量，分块表按 documentId 兜底清理
     * （向量删除失败不影响 FAILED 状态落库）
     */
    private void cleanupPartialData(Long documentId, List<String> insertedVectorIds) {
        if (insertedVectorIds != null && !insertedVectorIds.isEmpty()) {
            try {
                vectorStoreManager.removeChunks(insertedVectorIds);
            } catch (Exception e) {
                log.warn("清理部分向量数据失败（不影响失败状态记录）, documentId: {}, cause: {}",
                        documentId, e.getMessage());
            }
        }
        try {
            UpdateChain.of(AiKnowledgeDocChunk.class)
                    .eq(AiKnowledgeDocChunk::getDocumentId, documentId)
                    .remove();
        } catch (Exception e) {
            log.warn("清理部分分块数据失败（不影响失败状态记录）, documentId: {}, cause: {}",
                    documentId, e.getMessage());
        }
    }

    private void updateStatus(Long documentId, AiDocParseStatusEnum status, String failReason) {
        UpdateChain.of(AiKnowledgeDoc.class)
                .set(AiKnowledgeDoc::getParseStatus, status.getCode())
                .set(AiKnowledgeDoc::getParseFailReason, failReason)
                .eq(AiKnowledgeDoc::getId, documentId)
                .update();
    }
}
