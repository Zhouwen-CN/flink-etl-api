package com.etl.api.ai.service.manager;

import com.etl.api.ai.domain.dto.AiMcpServerConfigDTO;
import com.etl.api.ai.domain.entity.AiBotMcpTool;
import com.etl.api.ai.domain.entity.AiMcpServer;
import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.domain.validator.AiMcpConfigValidator;
import com.etl.api.ai.exception.AiMcpConfigException;
import com.etl.api.ai.service.AiMcpToolService;
import com.mybatisflex.core.query.QueryChain;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.service.tool.AiServiceTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
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
 * <p>知识库检索不走本类（原 search_knowledge_base 工具已下线）：RAG 管道经
 * {@link AiContentRetrieverManager} 组装 RetrievalAugmentor 注入检索内容。
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

    private final AiMcpConfigValidator configValidator;

    private final HttpClient aiHttpClient;

    /**
     * 组装 bot 的聊天工具集合（MCP 工具）。
     *
     * <p>bot 未挂载/挂载失效时返回空 List；单条工具构建失败（被删/配置失效/重名）
     * 跳过并 warn，其余照常注册；全部不可用时返回空 List，不阻断聊天。
     *
     * @param mcpToolList bot id
     * @return 注册给 LLM 的工具集合，无可用工具时为空 List
     */
    public List<AiServiceTool> build(List<AiMcpTool> mcpToolList) {
        val tools = new ArrayList<AiServiceTool>();
        if (CollectionUtils.isEmpty(mcpToolList)) {
            return tools;
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
                        mcpToolList, tool.getId(), tool.getServerId());
                continue;
            }

            ToolSpecification spec;
            try {
                // 缓存的 input_schema 即 ToolSpecification.toJson() 的产物，反序列化还原
                spec = ToolSpecification.fromJson(tool.getInputSchema());
            } catch (Exception e) {
                log.warn("MCP工具input_schema解析失败，跳过, botId: {}, toolId: {}, name: {}",
                        mcpToolList, tool.getId(), tool.getName(), e);
                continue;
            }
            if (!seenNames.add(spec.name())) {
                log.warn("MCP工具重名，仅保留首个, botId: {}, name: {}", mcpToolList, spec.name());
                continue;
            }

            val outputTemplate = tool.getOutputTemplate();
            tools.add(AiServiceTool.builder()
                    .toolSpecification(spec)
                    .toolExecutor((ToolExecutionRequest request, Object memoryId) ->
                            executeMcpTool(configJson, outputTemplate, request))
                    .build());
        }

        return tools;
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

    private McpClient buildClient(AiMcpServerConfigDTO serverConfig) {
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
    public List<ToolSpecification> fetchTools(AiMcpServerConfigDTO serverConfig) {
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
