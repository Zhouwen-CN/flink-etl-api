package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.dto.McpServerConfigDTO;
import com.etl.api.ai.domain.entity.AiBotMcpTool;
import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.domain.form.AiMcpToolUpdateForm;
import com.etl.api.ai.domain.vo.AiMcpToolVO;
import com.etl.api.ai.exception.AiMcpConfigException;
import com.etl.api.ai.mapper.AiBotMcpToolMapper;
import com.etl.api.ai.mapper.AiMcpToolMapper;
import com.etl.api.ai.service.AiMcpToolService;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.http.client.HttpClient;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * AI MCP工具表 服务层实现。
 *
 * <p>同步工具：临时连接流程 StreamableHttpMcpTransport（自动探测 SSE / Streamable HTTP 协议，
 * 支持 customHeaders）→ DefaultMcpClient → listTools() → close()，客户端不驻留。
 * 建连（transport timeout + initialization timeout）与 listTools 各自超时 5s。
 *
 * @author chen
 * @since 2026-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiMcpToolServiceImpl extends ServiceImpl<AiMcpToolMapper, AiMcpTool> implements AiMcpToolService {

    /**
     * 建连 / 初始化 / listTools 各自的超时时间（PRD F1.6）
     */
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final AiMcpToolMapper mcpToolMapper;
    private final AiBotMcpToolMapper botMcpToolMapper;
    private final HttpClient aiHttpClient;

    @Override
    public PageVO<AiMcpToolVO> getPage(Integer currentPage, Integer pageSize, Long serverId, String searchName) {
        val page = this.queryChain()
                .eq(AiMcpTool::getServerId, serverId)
                .like(AiMcpTool::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), AiMcpToolVO.class);
        return PageVO.from(page);
    }

    @Override
    public ResponseVO<Void> updateAiMcpTool(AiMcpToolUpdateForm form) {
        val entity = this.getById(form.getId());
        if (entity == null) {
            return ResponseVO.recordNotFoundError("ID", form.getId());
        }

        // 仅用户可配置字段；outputTemplate 传null表示清空（updateById 不忽略null，其余字段为已查出原值）
        entity.setOutputTemplate(form.getOutputTemplate());
        this.updateById(entity, false);
        log.info("更新MCP工具, toolId: {}, serverId: {}", entity.getId(), entity.getServerId());
        return ResponseVO.ok();
    }

    @Override
    public void syncTools(Long serverId, McpServerConfigDTO serverConfig) {
        List<ToolSpecification> specs = fetchTools(serverConfig);

        val existing = mcpToolMapper.selectListByQuery(
                        QueryWrapper.create().eq(AiMcpTool::getServerId, serverId)
                )
                .stream()
                .collect(HashMap<String, AiMcpTool>::new, (m, t) -> m.put(t.getName(), t), Map::putAll);

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
    private List<ToolSpecification> fetchTools(McpServerConfigDTO serverConfig) {
        // transport 级 timeout 与显式 httpClient 互斥，超时统一配置在 AiHttpClientConfig
        val transportBuilder = StreamableHttpMcpTransport.builder()
                .url(serverConfig.getUrl().trim())
                .httpClient(aiHttpClient);
        val headers = serverConfig.getHeaders();
        if (headers != null && !headers.isEmpty()) {
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
                throw new AiMcpConfigException("同步MCP工具失败: 连接或拉取工具清单超时（5s）");
            }
            throw new AiMcpConfigException("同步MCP工具失败（连接或拉取工具清单异常）: " + cause.getMessage());
        }
    }
}
