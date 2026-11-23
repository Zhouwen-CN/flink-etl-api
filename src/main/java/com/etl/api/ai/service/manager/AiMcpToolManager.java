package com.etl.api.ai.service.manager;

import com.etl.api.ai.domain.dto.McpServerConfigDTO;
import com.etl.api.ai.domain.entity.AiBotKnowledge;
import com.etl.api.ai.domain.entity.AiBotMcpTool;
import com.etl.api.ai.domain.entity.AiKnowledge;
import com.etl.api.ai.domain.entity.AiMcpServer;
import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.domain.validator.AiMcpConfigValidator;
import com.etl.api.ai.exception.AiMcpConfigException;
import com.etl.api.ai.service.AiMcpToolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryChain;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.tool.AiServiceTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * MCP 工具管理器：收口 MCP 临时建连（目录同步 {@link #fetchTools} / 聊天执行
 * {@link #buildClient}），并为 bot 组装注册给 LLM 的工具集合，组装单一入口 {@link #build}。
 *
 * <p>MCP 临时建连的统一收口点：聊天执行工具、{@link AiMcpToolService} 同步工具清单
 * （{@link #fetchTools}）共用同一套 transport 构建与超时配置，客户端均不驻留。
 * MCP 工具清单来自本地缓存（input_schema 反序列化，不实时 listTools）。
 *
 * <p>知识库检索工具部分（自研工具，非 MCP）：把 bot 绑定的知识库包装成
 * search_knowledge_base，LLM 自主决定何时检索、query 怎么改写；检索执行复用
 * {@link AiVectorStoreManager#buildContentRetriever}（各自携带该库的 embeddingModel
 * 与 recall_top_k/min_score 配置），结果带来源文档名与相似度分数供 LLM 引用。
 *
 * <p>本类位于 service.manager 包，不参与 service.impl 包事务切面；工具执行器跑在
 * langchain4j 工具循环内，参数解析失败/调用异常均返回错误文本交给 LLM 自行决策。
 *
 * @author chen
 * @since 2026-11-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiMcpToolManager {

    /**
     * 建连 / 初始化超时（与 AiMcpToolServiceImpl 一致）
     */
    private static final Duration MCP_INIT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * 工具执行超时（工具实际执行耗时远大于建连，单独放宽）
     */
    private static final Duration MCP_TOOL_EXECUTION_TIMEOUT = Duration.ofSeconds(60);

    /**
     * output_template 中的结果占位符
     */
    private static final String OUTPUT_TEMPLATE_PLACEHOLDER = "{result}";

    /**
     * 知识库检索工具名（固定；注册前保证与 MCP 工具名不冲突）
     */
    private static final String KNOWLEDGE_TOOL_NAME = "search_knowledge_base";

    /**
     * 检索工具描述（决定 LLM 是否调用与何时调用，需明确触发时机与兜底行为）
     */
    private static final String KNOWLEDGE_TOOL_DESCRIPTION = """
            搜索该助手绑定的知识库文档内容。当用户提问涉及文档、资料、制度、数据说明等知识库内容时，\
            必须先调用本工具获取依据再回答；检索不到相关信息时如实告知，不要编造。\
            """;

    /**
     * query 参数缺失/非法时的错误回传文本（交 LLM 重试）
     */
    private static final String INVALID_QUERY_MESSAGE = "参数错误: query 不能为空，请提供检索问题或关键词";

    /**
     * 多知识库合并召回的全局上限：各库各自按 recall_top_k 召回，绑定的库越多总量越大，
     * 合并后必须截断，防止工具结果把模型上下文撑爆
     */
    @Value("${custom.ai.knowledge-max-results:10}")
    private Integer knowledgeMaxResults = 10;

    private final AiVectorStoreManager vectorStoreManager;
    private final AiMcpConfigValidator configValidator;
    private final HttpClient aiHttpClient;
    private final ObjectMapper objectMapper;

    /**
     * 组装 bot 的聊天工具集合（MCP 工具 + 知识库检索工具）。
     *
     * <p>单条工具构建失败（被删/配置失效/重名）跳过并 warn，其余照常注册；
     * 全部不可用时返回空 List，不阻断聊天。
     *
     * @param botId bot id
     * @return 注册给 LLM 的工具集合，无可用工具时为空 List
     */
    public List<AiServiceTool> build(Long botId) {
        val tools = new ArrayList<AiServiceTool>();
        appendMcpTools(botId, tools);
        appendKnowledgeSearchTool(botId, tools);
        return tools;
    }

    /**
     * 追加 MCP 工具：bot 未挂载/挂载失效时跳过
     */
    private void appendMcpTools(Long botId, List<AiServiceTool> result) {
        val toolIds = QueryChain.of(AiBotMcpTool.class)
                .select(AiBotMcpTool::getToolId)
                .eq(AiBotMcpTool::getBotId, botId)
                .listAs(Long.class);
        if (CollectionUtils.isEmpty(toolIds)) {
            return;
        }
        val mcpToolList = QueryChain.of(AiMcpTool.class)
                .in(AiMcpTool::getId, toolIds)
                .list();
        if (CollectionUtils.isEmpty(mcpToolList)) {
            return;
        }

        val serverIds = mcpToolList.stream().map(AiMcpTool::getServerId).distinct().toList();
        val configByServerId = QueryChain.of(AiMcpServer.class)
                .in(AiMcpServer::getId, serverIds)
                .list()
                .stream()
                .collect(
                        HashMap<Long, String>::new,
                        (m, s) -> m.put(s.getId(), s.getConfigJson()),
                        Map::putAll
                );

        // 工具名在单次请求内必须唯一，重名会导致框架按名分发时错乱
        val seenNames = new HashSet<String>();
        for (val tool : mcpToolList) {
            val configJson = configByServerId.get(tool.getServerId());
            if (!StringUtils.hasText(configJson)) {
                log.warn("MCP工具所属服务配置缺失，跳过, botId: {}, toolId: {}, serverId: {}",
                        botId, tool.getId(), tool.getServerId());
                continue;
            }

            ToolSpecification spec;
            try {
                // 缓存的 input_schema 即 ToolSpecification.toJson() 的产物，反序列化还原
                spec = ToolSpecification.fromJson(tool.getInputSchema());
            } catch (Exception e) {
                log.warn("MCP工具input_schema解析失败，跳过, botId: {}, toolId: {}, name: {}",
                        botId, tool.getId(), tool.getName(), e);
                continue;
            }
            if (!seenNames.add(spec.name())) {
                log.warn("MCP工具重名，仅保留首个, botId: {}, name: {}", botId, spec.name());
                continue;
            }

            val outputTemplate = tool.getOutputTemplate();
            result.add(AiServiceTool.builder()
                    .toolSpecification(spec)
                    .toolExecutor((ToolExecutionRequest request, Object memoryId) ->
                            executeMcpTool(configJson, outputTemplate, request))
                    .build());
        }
    }

    /**
     * 追加知识库检索工具（绑定了知识库的 bot 才注册；工具名与已注册 MCP 工具重名时放弃注册）
     */
    private void appendKnowledgeSearchTool(Long botId, List<AiServiceTool> result) {
        val knowledgeIds = QueryChain.of(AiBotKnowledge.class)
                .select(AiBotKnowledge::getKnowledgeId)
                .eq(AiBotKnowledge::getBotId, botId)
                .listAs(Long.class);
        if (CollectionUtils.isEmpty(knowledgeIds)) {
            return;
        }
        val hasConflict = result.stream()
                .anyMatch(tool -> KNOWLEDGE_TOOL_NAME.equals(tool.toolSpecification().name()));
        if (hasConflict) {
            log.warn("知识库检索工具名与MCP工具重名，放弃注册, botId: {}", botId);
            return;
        }

        // 单个知识库检索器构建失败（向量库配置失效等）跳过该库，其余照常；全部失效时不注册。
        val knowledgeList = QueryChain.of(AiKnowledge.class)
                .in(AiKnowledge::getId, knowledgeIds)
                .list();
        val retrieverList = new ArrayList<EmbeddingStoreContentRetriever>();
        for (val knowledge : knowledgeList) {
            try {
                retrieverList.add(vectorStoreManager.buildContentRetriever(knowledge));
            } catch (Exception e) {
                log.warn("知识库检索器构建失败，跳过该知识库, knowledgeId: {}, cause: {}",
                        knowledge.getId(), e.getMessage());
            }
        }
        if (retrieverList.isEmpty()) {
            return;
        }

        result.add(AiServiceTool.builder()
                .toolSpecification(knowledgeToolSpec())
                .toolExecutor((ToolExecutionRequest request, Object memoryId) ->
                        executeKnowledgeSearch(request, retrieverList))
                .build());
    }

    private ToolSpecification knowledgeToolSpec() {
        return ToolSpecification.builder()
                .name(KNOWLEDGE_TOOL_NAME)
                .description(KNOWLEDGE_TOOL_DESCRIPTION)
                .parameters(JsonObjectSchema.builder()
                        .addStringProperty("query", "用于向量检索的问题或关键词")
                        .required("query")
                        .build())
                .build();
    }

    /**
     * 执行多知识库检索：各库先按自身 recall_top_k/min_score 召回，合并后按 score 全局
     * 降序截断到 knowledgeMaxResults 条（单库绑定不会触发截断）；异常兜底为错误文本回传 LLM
     */
    private String executeKnowledgeSearch(ToolExecutionRequest request,
                                          List<EmbeddingStoreContentRetriever> retrieverList) {
        val query = extractQuery(request.arguments());
        if (!StringUtils.hasText(query)) {
            return INVALID_QUERY_MESSAGE;
        }
        try {
            val contents = new ArrayList<Content>();
            for (val retriever : retrieverList) {
                contents.addAll(retriever.retrieve(Query.from(query)));
            }
            return formatContents(rankContents(contents));
        } catch (Exception e) {
            log.warn("知识库检索失败, tool: {}, error: {}", KNOWLEDGE_TOOL_NAME, e.getMessage());
            return "知识库检索失败: " + e.getMessage();
        }
    }

    /**
     * 跨库合并排序：score 全局降序截断到 KNOWLEDGE_MAX_RESULTS。
     * 注意各库可用不同 embedding model，score 量纲不同，跨库排序是启发式近似；
     * 单 score 缺失按 0 兜底排末尾（EmbeddingStoreContentRetriever 正常都会写入 SCORE）。
     */
    private List<Content> rankContents(List<Content> contents) {
        return contents.stream()
                .sorted(Comparator.comparingDouble(AiMcpToolManager::scoreOf).reversed())
                .limit(knowledgeMaxResults)
                .toList();
    }

    private static double scoreOf(Content content) {
        val score = content.metadata().get(ContentMetadata.SCORE);
        return score instanceof Number number ? number.doubleValue() : 0.0;
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
     * 结果格式化：来源文档名与相似度分数随 metadata 固化在向量数据上（upsertChunk 写入），
     * 直接读取即可，无需来源反查
     */
    private String formatContents(List<Content> contents) {
        if (contents.isEmpty()) {
            return "知识库中未检索到相关内容。";
        }

        val sb = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            val content = contents.get(i);
            val segment = content.textSegment();
            val fileName = segment.metadata().getString(AiVectorStoreManager.METADATA_DOCUMENT_NAME);
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
     * 临时建连执行 MCP 工具调用，结果按 output_template 包装；失败返回错误文本交给 LLM 自行决策
     */
    private String executeMcpTool(String configJson, String outputTemplate, ToolExecutionRequest request) {
        try (McpClient client = buildClient(configValidator.validate(configJson))) {
            val toolResult = client.executeTool(request);
            val text = toolResult.resultText();
            val wrapped = StringUtils.hasText(outputTemplate)
                    ? outputTemplate.replace(OUTPUT_TEMPLATE_PLACEHOLDER, text)
                    : text;
            return toolResult.isError() ? "工具执行出错: " + wrapped : wrapped;
        } catch (Exception e) {
            log.warn("MCP工具调用失败, tool: {}, error: {}", request.name(), e.getMessage());
            return "工具调用失败: " + e.getMessage();
        }
    }

    private McpClient buildClient(McpServerConfigDTO serverConfig) {
        // transport 级 timeout 与显式 httpClient 互斥，超时统一配置在 AiHttpClientConfig
        val transportBuilder = StreamableHttpMcpTransport.builder()
                .url(serverConfig.getUrl().trim())
                .httpClient(aiHttpClient);
        val headers = serverConfig.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            transportBuilder.customHeaders(headers);
        }

        return new DefaultMcpClient.Builder()
                .transport(transportBuilder.build())
                .initializationTimeout(MCP_INIT_TIMEOUT)
                .toolExecutionTimeout(MCP_TOOL_EXECUTION_TIMEOUT)
                .build();
    }

    /**
     * 建立临时连接拉取工具清单，用后即关；供 {@link AiMcpToolService} 工具目录同步使用。
     * listTools 与建连共用 MCP_INIT_TIMEOUT 超时，失败抛 AiMcpConfigException 由调用方透出。
     */
    public List<ToolSpecification> fetchTools(McpServerConfigDTO serverConfig) {
        try (McpClient client = buildClient(serverConfig)) {
            return CompletableFuture.supplyAsync(client::listTools)
                    .get(MCP_INIT_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            val cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof TimeoutException) {
                throw new AiMcpConfigException("同步MCP工具失败: 连接或拉取工具清单超时（5s）");
            }
            throw new AiMcpConfigException("同步MCP工具失败（连接或拉取工具清单异常）: " + cause.getMessage());
        }
    }
}
