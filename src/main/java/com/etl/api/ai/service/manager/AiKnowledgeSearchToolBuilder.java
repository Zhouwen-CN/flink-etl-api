package com.etl.api.ai.service.manager;

import com.etl.api.ai.domain.entity.AiKnowledgeDoc;
import com.etl.api.ai.mapper.AiKnowledgeDocMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryChain;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.tool.AiServiceTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 知识库检索工具构建器：把 bot 绑定的知识库包装成注册给 LLM 的自研工具
 * （search_knowledge_base），LLM 自主决定何时检索、query 怎么改写。
 *
 * <p>检索执行复用 {@link AiVectorStoreManager#buildContentRetriever}（各自携带该库的
 * embeddingModel 与 recall_top_k/min_score 配置，store 集合按知识库独立派生，无需 metadata 过滤）；
 * 结果带来源文档名（document_id 反查文档表）与相似度分数，供 LLM 引用。
 *
 * <p>本类位于 service.manager 包，不参与 service.impl 包事务切面；工具执行器跑在
 * langchain4j 工具循环内，参数解析失败/检索异常均返回错误文本交给 LLM 自行决策
 * （与 {@code AiBotServiceImpl#executeMcpTool} 失败语义一致）。
 *
 * @author chen
 * @since 2026-11-22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiKnowledgeSearchToolBuilder {

    /**
     * 检索工具名（固定；buildChatTools 侧保证与 MCP 工具名不冲突）
     */
    public static final String TOOL_NAME = "search_knowledge_base";

    /**
     * 工具描述（决定 LLM 是否调用与何时调用，需明确触发时机与兜底行为）
     */
    private static final String TOOL_DESCRIPTION = """
            搜索该助手绑定的知识库文档内容。当用户提问涉及文档、资料、制度、数据说明等知识库内容时，\
            必须先调用本工具获取依据再回答；检索不到相关信息时如实告知，不要编造。\
            """;

    /**
     * query 参数缺失/非法时的错误回传文本（交 LLM 重试）
     */
    private static final String INVALID_QUERY_MESSAGE = "参数错误: query 不能为空，请提供检索问题或关键词";

    private final AiVectorStoreManager vectorStoreManager;
    private final ObjectMapper objectMapper;

    /**
     * 构建知识库检索工具。
     *
     * <p>单个知识库构建失败（被删/向量库配置失效）跳过并 warn，其余库照常注册；
     * 全部失效时返回 empty，不阻断 bot 其他工具。
     *
     * @param knowledgeIds bot 绑定的知识库 id 列表（调用方保证非空）
     * @return 检索工具；无可用知识库时返回 empty
     */
    public Optional<AiServiceTool> build(List<Long> knowledgeIds) {
        val retrievers = new LinkedHashMap<Long, EmbeddingStoreContentRetriever>();
        for (val knowledgeId : knowledgeIds) {
            try {
                retrievers.put(knowledgeId, vectorStoreManager.buildContentRetriever(knowledgeId));
            } catch (Exception e) {
                log.warn("知识库检索器构建失败，跳过该知识库, knowledgeId: {}, cause: {}",
                        knowledgeId, e.getMessage());
            }
        }
        if (retrievers.isEmpty()) {
            return Optional.empty();
        }

        val spec = ToolSpecification.builder()
                .name(TOOL_NAME)
                .description(TOOL_DESCRIPTION)
                .parameters(JsonObjectSchema.builder()
                        .addStringProperty("query", "用于向量检索的问题或关键词")
                        .required("query")
                        .build())
                .build();

        return Optional.of(AiServiceTool.builder()
                .toolSpecification(spec)
                .toolExecutor((ToolExecutionRequest request, Object memoryId) ->
                        executeKnowledgeSearch(request, retrievers))
                .build());
    }

    /**
     * 执行多知识库检索并合并结果；异常兜底为错误文本回传 LLM
     */
    private String executeKnowledgeSearch(ToolExecutionRequest request,
                                          Map<Long, EmbeddingStoreContentRetriever> retrievers) {
        val query = extractQuery(request.arguments());
        if (!StringUtils.hasText(query)) {
            return INVALID_QUERY_MESSAGE;
        }
        try {
            val contents = new ArrayList<Content>();
            for (val retriever : retrievers.values()) {
                contents.addAll(retriever.retrieve(Query.from(query)));
            }
            return formatContents(contents);
        } catch (Exception e) {
            log.warn("知识库检索失败, tool: {}, error: {}", TOOL_NAME, e.getMessage());
            return "知识库检索失败: " + e.getMessage();
        }
    }

    /**
     * 从工具调用参数 JSON 中解析 query
     */
    private String extractQuery(String argumentsJson) {
        if (!StringUtils.hasText(argumentsJson)) {
            return null;
        }
        try {
            val node = objectMapper.readTree(argumentsJson).get("query");
            return node == null || node.isNull() ? null : node.asText();
        } catch (Exception e) {
            log.warn("检索工具参数解析失败, arguments: {}, cause: {}", argumentsJson, e.getMessage());
            return null;
        }
    }

    /**
     * 结果格式化：带来源文档名与相似度分数，供 LLM 引用与回答时标注出处
     */
    private String formatContents(List<Content> contents) {
        if (contents.isEmpty()) {
            return "知识库中未检索到相关内容。";
        }

        val fileNameByDocumentId = loadFileNames(contents);
        val sb = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            val content = contents.get(i);
            val segment = content.textSegment();
            val documentId = segment.metadata().getString(AiVectorStoreManager.METADATA_DOCUMENT_ID);
            val fileName = fileNameByDocumentId.getOrDefault(documentId, "未知文档(id=" + documentId + ")");
            val score = content.metadata().get(ContentMetadata.SCORE);
            sb.append("[来源").append(i + 1).append("] 文档: ").append(fileName);
            if (score != null) {
                sb.append(" (score=").append(score).append(")");
            }
            sb.append('\n').append(segment.text()).append("\n\n");
        }
        return sb.toString().strip();
    }

    /**
     * 按命中内容 metadata 中的 document_id 批量反查文档名（缺 metadata 的内容降级为未知文档）
     */
    private Map<String, String> loadFileNames(List<Content> contents) {
        val documentIds = contents.stream()
                .map(content -> content.textSegment().metadata()
                        .getString(AiVectorStoreManager.METADATA_DOCUMENT_ID))
                .filter(StringUtils::hasText)
                .map(Long::valueOf)
                .distinct()
                .toList();
        if (documentIds.isEmpty()) {
            return Map.of();
        }
        return QueryChain.of(AiKnowledgeDoc.class)
                .select(AiKnowledgeDoc::getId, AiKnowledgeDoc::getFileName)
                .in(AiKnowledgeDoc::getId, documentIds)
                .list()
                .stream()
                .collect(
                        HashMap::new,
                        (map, doc) -> map.put(String.valueOf(doc.getId()), doc.getFileName()),
                        Map::putAll
                );
    }
}
