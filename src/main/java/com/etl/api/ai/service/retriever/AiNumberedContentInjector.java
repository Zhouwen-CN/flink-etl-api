package com.etl.api.ai.service.retriever;

import com.etl.api.ai.domain.dto.AiRetrievalContentDTO;
import com.etl.api.ai.service.manager.AiVectorStoreManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.injector.ContentInjector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cherry Studio 风格注入器：将检索资料转为 {@link AiRetrievalContentDTO} 列表，
 * 序列化为 JSON 数组经提示词模板注入 user 消息，并要求模型在正文相应位置用 [编号]
 * 标注引用（多来源并列标注如 [1][2]）。
 *
 * <p>转换结果同时写入构造时传入的 retrievalContents 列表（调用方持有），
 * 供 onCompleteResponse 时随 toAttributes 落库——提示词与落库是同一份数据，
 * 编号与正文 [n] 天然对齐。
 *
 * <p>正文下标由提示词驱动，模型可能偶发漏标/编造编号，前端按越界忽略兜底。
 *
 * @author chen
 * @since 2026-11-27
 */
@Slf4j
@RequiredArgsConstructor
public class AiNumberedContentInjector implements ContentInjector {

    /**
     * 中文引用规则模板（{{contents}} 为 JSON 数组，仅含单个花括号，不与 {{var}} 语法冲突）
     */
    static final PromptTemplate PROMPT_TEMPLATE = PromptTemplate.from("""
            请根据参考资料回答用户问题。

            ## 引用规则：
            - 引用资料时，请在相应句子的末尾标注引用编号。
            - 使用 [编号] 格式引用对应资料，如 [1]。
            - 如果某句话来自多个资料，请列出所有相关编号，如 [1][2]。不要把引用集中在回答末尾，而是在对应位置标注。
            - 如果所有参考资料都与用户问题无关，请基于自身知识回答。

            ## 我的问题是：
            {{userMessage}}

            ## 参考资料：

            ```json
            {{contents}}
            ```""");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 调用方持有的引用列表（需线程安全：inject 在 RAG 异步阶段执行）
     */
    private final List<AiRetrievalContentDTO> retrievalContents;

    @Override
    public ChatMessage inject(List<Content> contents, ChatMessage chatMessage) {
        if (contents.isEmpty()) {
            return chatMessage;
        }

        List<AiRetrievalContentDTO> dtos = new ArrayList<>(contents.size());
        for (int i = 0; i < contents.size(); i++) {
            try {
                AiRetrievalContentDTO dto = toDto(i + 1, contents.get(i));
                dtos.add(dto);
            } catch (Exception e) {
                log.warn("RAG 引用转换失败, number={}", i + 1, e);
            }
        }
        if (dtos.isEmpty()) {
            return chatMessage;
        }
        retrievalContents.addAll(dtos);

        String json;
        try {
            json = MAPPER.writeValueAsString(dtos);
        } catch (Exception e) {
            log.warn("参考资料 JSON 序列化失败，降级为不注入参考资料", e);
            return chatMessage;
        }

        Prompt prompt = PROMPT_TEMPLATE.apply(Map.of(
                "userMessage", ((UserMessage) chatMessage).singleText(),
                "contents", json));

        return ((UserMessage) chatMessage).toBuilder()
                .contents(List.of(TextContent.from(prompt.text())))
                .build();
    }

    private AiRetrievalContentDTO toDto(int number, Content content) {
        TextSegment segment = content.textSegment();
        Metadata textSegmentMetadata = segment.metadata();
        String text = segment.text();

        AiRetrievalContentDTO dto = new AiRetrievalContentDTO();
        dto.setNumber(number);
        if (textSegmentMetadata.containsKey(AiVectorStoreManager.METADATA_DOCUMENT_NAME)) {
            // 知识库源：文档名即标题
            dto.setTitle(textSegmentMetadata.getString(AiVectorStoreManager.METADATA_DOCUMENT_NAME));
            dto.setContent(text);

            // score
            Map<ContentMetadata, Object> contentMetadata = content.metadata();
            Object scoreObj = contentMetadata.get(ContentMetadata.SCORE);
            if (scoreObj instanceof Double score) {
                dto.setScore(score);
            }

            // embedding id
            Object embeddingIdObj = contentMetadata.get(ContentMetadata.EMBEDDING_ID);
            dto.setEmbeddingId(embeddingIdObj == null ? null : String.valueOf(embeddingIdObj));
        } else if (textSegmentMetadata.containsKey("url")) {
            // web 源：标题为文本首行（单换行分隔），其余为内容
            dto.setUrl(textSegmentMetadata.getString("url"));
            dto.setScore(textSegmentMetadata.getDouble("score"));

            int separator = text == null ? -1 : text.indexOf("\n");
            if (separator > 0) {
                dto.setTitle(text.substring(0, separator));
                dto.setContent(text.substring(separator + 1));
            } else {
                dto.setContent(text);
            }
        } else {
            // 未知形态：仅内容兜底
            dto.setContent(text);
        }
        return dto;
    }
}
