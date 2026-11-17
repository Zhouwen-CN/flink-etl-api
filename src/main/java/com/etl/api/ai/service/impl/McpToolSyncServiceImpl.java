package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.entity.AiBotMcpTool;
import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.exception.McpConfigException;
import com.etl.api.ai.mapper.AiBotMcpToolMapper;
import com.etl.api.ai.mapper.AiMcpToolMapper;
import com.etl.api.ai.service.McpToolSyncService;
import com.fasterxml.jackson.databind.JsonNode;
import com.mybatisflex.core.query.QueryWrapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * MCP 工具同步服务实现。
 *
 * <p>临时连接流程：StreamableHttpMcpTransport（自动探测 SSE / Streamable HTTP 协议，
 * 支持 customHeaders）→ DefaultMcpClient → listTools() → close()，客户端不驻留。
 * 建连（transport timeout + initialization timeout）与 listTools 各自超时 5s。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolSyncServiceImpl implements McpToolSyncService {

    /**
     * 建连 / 初始化 / listTools 各自的超时时间（PRD F1.6）
     */
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final AiMcpToolMapper mcpToolMapper;
    private final AiBotMcpToolMapper botMcpToolMapper;

    @Override
    public void syncTools(Long serverId, JsonNode serverConfig) {
        List<ToolSpecification> specs = fetchTools(serverConfig);

        val mcpToolList = mcpToolMapper.selectListByQuery(QueryWrapper.create().eq(AiMcpTool::getServerId, serverId));
        val existing = Optional.ofNullable(mcpToolList)
                .map(list -> list.stream()
                        .collect(HashMap<String, AiMcpTool>::new, (m, t) -> m.put(t.getName(), t), Map::putAll)
                ).orElse(new HashMap<>());

        for (val spec : specs) {
            val description = spec.description();
            val inputSchema = spec.toJson();
            val old = existing.remove(spec.name());
            if (old == null) {
                mcpToolMapper.insert(AiMcpTool.builder()
                        .serverId(serverId)
                        .name(spec.name())
                        .description(description)
                        .inputSchema(inputSchema)
                        .build());
            } else {
                // 只覆盖同步类字段，不得覆盖用户配置的 output_template
                mcpToolMapper.update(AiMcpTool.builder()
                        .id(old.getId())
                        .description(description)
                        .inputSchema(inputSchema)
                        .build());
            }
        }

        // 服务端已消失的工具物理删除，并级联删除挂载行
        if (!existing.isEmpty()) {
            val staleIds = existing.values().stream().map(AiMcpTool::getId).toList();
            botMcpToolMapper.deleteByQuery(QueryWrapper.create().in(AiBotMcpTool::getToolId, staleIds));
            mcpToolMapper.deleteByQuery(QueryWrapper.create().in(AiMcpTool::getId, staleIds));
            log.info("MCP工具同步清理失效工具, serverId: {}, removedTools: {}", serverId, existing.keySet());
        }

        log.info("MCP工具同步完成, serverId: {}, toolCount: {}", serverId, specs.size());
    }

    /**
     * 建立临时连接拉取工具清单，用后即关。
     */
    private List<ToolSpecification> fetchTools(JsonNode serverConfig) {
        val url = serverConfig.path("url").asText();

        val transportBuilder = StreamableHttpMcpTransport.builder()
                .url(url.trim())
                .timeout(TIMEOUT);
        val headers = extractHeaders(serverConfig.get("headers"));
        if (!headers.isEmpty()) {
            transportBuilder.customHeaders(headers);
        }

        try (McpClient client = new DefaultMcpClient.Builder()
                .transport(transportBuilder.build())
                .initializationTimeout(TIMEOUT)
                .build()
        ) {
            return CompletableFuture.supplyAsync(client::listTools)
                    .get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            val cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof TimeoutException) {
                throw new McpConfigException("同步MCP工具失败: 连接或拉取工具清单超时（5s）");
            }
            throw new McpConfigException("同步MCP工具失败（连接或拉取工具清单异常）: " + cause.getMessage());
        }
    }

    /**
     * headers 节点转 Map（仅取文本值），缺失或空返回空 Map
     */
    private Map<String, String> extractHeaders(JsonNode headers) {
        val result = new LinkedHashMap<String, String>();
        if (headers != null && headers.isObject()) {
            for (val field : headers.properties()) {
                if (StringUtils.hasText(field.getKey()) && field.getValue().isTextual()) {
                    result.put(field.getKey(), field.getValue().asText());
                }
            }
        }
        return result;
    }
}
