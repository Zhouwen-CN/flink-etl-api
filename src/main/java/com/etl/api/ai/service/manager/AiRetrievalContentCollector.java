package com.etl.api.ai.service.manager;

import com.etl.api.ai.domain.dto.AiRetrievalContentDTO;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * RAG 引用采集器（每次聊天一个实例，非 Spring Bean）：录制代理在各源 retrieve 后调用
 * record 记录引用条目，onCompleteResponse 时随 done 事件与 attributes 回写一并输出。
 *
 * <p>两种源的内容形态（LC4j 1.23.0 源码核实）：
 * 知识库源 segment metadata 携带 document_name，Content metadata 携带 SCORE（Double）；
 * web 源 segment metadata 仅携带 url，标题拼在检索文本首行（WebSearchOrganicResult#toTextSegments）。
 *
 * @author chen
 * @since 2026-11-26
 */
@Getter
@Slf4j
public class AiRetrievalContentCollector {

    public static final String SOURCE_KNOWLEDGE_BASE = "knowledge_base";
    public static final String SOURCE_WEB_SEARCH = "web_search";
    private static final int SNIPPET_MAX_LENGTH = 200;

    private final List<AiRetrievalContentDTO> retrievalContentList = new CopyOnWriteArrayList<>();

    /**
     * 记录一次检索结果，单条解析失败跳过，不影响其余
     */
    public void record(String sourceName, List<Content> contents) {
        if (CollectionUtils.isEmpty(contents)) {
            return;
        }
        for (Content content : contents) {
            try {
                AiRetrievalContentDTO retrievalContentDTO = toRetrievalContent(sourceName, content);
                retrievalContentList.add(retrievalContentDTO);
            } catch (Exception e) {
                log.warn("RAG 引用采集失败, source={}", sourceName, e);
            }
        }
    }

    private AiRetrievalContentDTO toRetrievalContent(String sourceName, Content content) {
        TextSegment segment = content.textSegment();
        Metadata metadata = segment.metadata();
        String text = segment.text();

        AiRetrievalContentDTO dto = new AiRetrievalContentDTO();
        dto.setSource(sourceName);
        switch (sourceName) {
            case SOURCE_KNOWLEDGE_BASE -> {
                dto.setTitle(metadata.containsKey(AiVectorStoreManager.METADATA_DOCUMENT_NAME)
                        ? metadata.getString(AiVectorStoreManager.METADATA_DOCUMENT_NAME)
                        : null);
                dto.setScore(metadata.getDouble("score"));
                dto.setSnippet(truncate(text));
            }
            case SOURCE_WEB_SEARCH -> {
                dto.setUrl(metadata.getString("url"));
                dto.setScore(metadata.getDouble("score"));
                // 标题为文本首行，其余为摘要
                int separator = text == null ? -1 : text.indexOf("\n");
                if (separator > 0) {
                    dto.setTitle(text.substring(0, separator));
                    dto.setSnippet(truncate(text.substring(separator + 1)));
                } else {
                    dto.setSnippet(truncate(text));
                }
            }
            default -> dto.setSnippet(truncate(text));
        }
        return dto;
    }

    private String truncate(String text) {
        if (text == null || text.length() <= SNIPPET_MAX_LENGTH) {
            return text;
        }
        return text.substring(0, SNIPPET_MAX_LENGTH) + "...";
    }
}
