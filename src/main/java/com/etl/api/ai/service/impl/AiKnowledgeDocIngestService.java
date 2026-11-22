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
 * 向量 ID 格式 doc_{documentId}:chk_{chunkId}（AiVectorStoreManager 统一生成）。
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
     * chunk_size / overlap_size 空值兜底（对齐 DDL 默认值）
     */
    private static final int DEFAULT_CHUNK_SIZE = 800;
    private static final int DEFAULT_OVERLAP_SIZE = 200;

    /**
     * parse_fail_reason 列长度
     */
    private static final int MAX_FAIL_REASON_LENGTH = 1024;

    /**
     * Apache Tika 统一解析 pdf/docx/xlsx/md/txt（自动探测内容类型，无需按 file_type 分发解析器）
     */
    private final ApacheTikaDocumentParser tikaParser = new ApacheTikaDocumentParser();

    /**
     * 分块单条插入需回填自增 id（供失败清理），无链式 API 等价物，保留 mapper
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
     * 异步解析流水线：读 blob → Tika 提取全文 → recursive 分块 → 逐块（落库拿自增 id → 向量化 → 写向量库，
     * chunk.id 作向量 ID）→ 更新 parse_status=SUCCESS + content。
     */
    private void parseAndVectorize(Long documentId) {
        // 链式查询为 SELECT *，会带出 file_content 大字段（解析必需）
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

        // chunk.id 在落库后回填，用于失败时清理
        List<Long> insertedChunkIds = new ArrayList<>();
        try {
            updateStatus(knowledgeDoc.getId(), AiDocParseStatusEnum.PROCESSING, null);

            // 1. 解析全文
            String text = parse(knowledgeDoc.getFileContent(), knowledgeDoc.getFileType());
            if (text == null || text.isBlank()) {
                throw new IllegalStateException("文档解析结果为空，未产生任何文本");
            }

            // 2. recursive 分块（按字符计，chunk_size/overlap_size 取知识库配置）
            int chunkSize = knowledge.getChunkSize() != null ? knowledge.getChunkSize() : DEFAULT_CHUNK_SIZE;
            int overlapSize = knowledge.getOverlapSize() != null ? knowledge.getOverlapSize() : DEFAULT_OVERLAP_SIZE;
            List<TextSegment> splitSegments =
                    DocumentSplitters.recursive(chunkSize, overlapSize).split(Document.from(text));
            if (splitSegments.isEmpty()) {
                throw new IllegalStateException("分块结果为空，请检查文档内容与分块参数");
            }

            // 3. 逐块流水线：落库回填自增 id（insertBatch 不回填主键）→ 向量化 → 写向量库（id 即向量文档 ID）
            //    model/store 由 manager 内部解析，配置缺失在首块 upsert 即失败，insertedChunkIds 兜底清理
            for (TextSegment splitSegment : splitSegments) {
                AiKnowledgeDocChunk chunk = AiKnowledgeDocChunk.builder()
                        .documentId(knowledgeDoc.getId())
                        .knowledgeId(knowledgeDoc.getKnowledgeId())
                        .content(splitSegment.text())
                        .build();
                chunkMapper.insert(chunk);
                val chunkId = chunk.getId();
                insertedChunkIds.add(chunkId);
                vectorStoreManager.upsertChunk(chunk, chunk.getContent());
            }

            // 4. 成功：固化全文 + SUCCESS
            UpdateChain.of(AiKnowledgeDoc.class)
                    .set(AiKnowledgeDoc::getContent, text)
                    .set(AiKnowledgeDoc::getParseStatus, AiDocParseStatusEnum.SUCCESS.getCode())
                    .eq(AiKnowledgeDoc::getId, knowledgeDoc.getId())
                    .update();
            log.info("文档解析向量化完成, documentId: {}, chunks: {}", knowledgeDoc.getId(), insertedChunkIds.size());
        } catch (Exception e) {
            log.error("文档解析向量化失败, documentId: {}", knowledgeDoc.getId(), e);
            cleanupPartialData(knowledgeDoc.getKnowledgeId(), knowledgeDoc.getId(), insertedChunkIds);
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
     * 失败清理：尽力删除已写入的分块与其向量（向量删除失败不影响 FAILED 状态落库）
     */
    private void cleanupPartialData(Long knowledgeId, Long documentId, List<Long> insertedChunkIds) {
        if (insertedChunkIds.isEmpty()) {
            return;
        }
        try {
            vectorStoreManager.removeChunks(knowledgeId, documentId, insertedChunkIds);
        } catch (Exception e) {
            log.warn("清理部分向量数据失败（不影响失败状态记录）, documentChunkIds: {}, cause: {}",
                    insertedChunkIds, e.getMessage());
        }
        try {
            UpdateChain.of(AiKnowledgeDocChunk.class)
                    .in(AiKnowledgeDocChunk::getId, insertedChunkIds)
                    .remove();
        } catch (Exception e) {
            log.warn("清理部分分块数据失败（不影响失败状态记录）, documentChunkIds: {}, cause: {}",
                    insertedChunkIds, e.getMessage());
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
