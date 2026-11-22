package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.convert.AiBotConvert;
import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.entity.AiBotKnowledge;
import com.etl.api.ai.domain.entity.AiBotMcpTool;
import com.etl.api.ai.domain.entity.AiKnowledge;
import com.etl.api.ai.domain.entity.AiMcpServer;
import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.domain.form.AiBotCreateForm;
import com.etl.api.ai.domain.form.AiBotUpdateForm;
import com.etl.api.ai.domain.validator.AiMcpConfigValidator;
import com.etl.api.ai.mapper.AiBotKnowledgeMapper;
import com.etl.api.ai.mapper.AiBotMapper;
import com.etl.api.ai.mapper.AiBotMcpToolMapper;
import com.etl.api.ai.service.AiBotService;
import com.etl.api.ai.service.manager.AiKnowledgeSearchToolBuilder;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.LambdaGetter;
import com.mybatisflex.spring.service.impl.ServiceImpl;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AI Bot表 服务层实现。
 *
 * <p>聊天工具构建：工具清单来自本地缓存（input_schema 反序列化，不实时 listTools）；
 * 执行时临时建连调用远端工具（与 AiMcpToolService 同步工具同一连接模式，用后即关）。
 *
 * @author chen
 * @since 2026-11-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiBotServiceImpl extends ServiceImpl<AiBotMapper, AiBot> implements AiBotService {

    /**
     * 建连 / 初始化超时（与 AiMcpToolServiceImpl 一致）
     */
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    /**
     * 工具执行超时（工具实际执行耗时远大于建连，单独放宽）
     */
    private static final Duration TOOL_EXECUTION_TIMEOUT = Duration.ofSeconds(60);

    /**
     * output_template 中的结果占位符
     */
    private static final String OUTPUT_TEMPLATE_PLACEHOLDER = "{result}";

    /**
     * 挂载表批量插入无链式 API（insertBatch），保留 mapper
     */
    private final AiBotMcpToolMapper botMcpToolMapper;
    private final AiBotKnowledgeMapper botKnowledgeMapper;
    private final AiKnowledgeSearchToolBuilder knowledgeSearchToolBuilder;
    private final AiMcpConfigValidator configValidator;
    private final HttpClient aiHttpClient;

    @Override
    public ResponseVO<Void> addAiBot(AiBotCreateForm form) {
        val exists = this.queryChain()
                .eq(AiBot::getName, form.getName())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError("Name", form.getName());
        }
        // 挂载引用先校验后落库，避免校验失败时留下无挂载的半截 bot 记录
        val refsError = validateBotRefs(form.getToolIds(), form.getKnowledgeIds());
        if (refsError != null) {
            return refsError;
        }
        val entity = AiBotConvert.INSTANCE.convert(form);
        this.save(entity);
        insertBotMcpTools(entity.getId(), form.getToolIds(), false);
        insertBotKnowledge(entity.getId(), form.getKnowledgeIds(), false);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> updateAiBot(AiBotUpdateForm form) {
        val exists = this.queryChain()
                .eq(AiBot::getName, form.getName())
                .ne(AiBot::getId, form.getId())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError("Name", form.getName());
        }
        val refsError = validateBotRefs(form.getToolIds(), form.getKnowledgeIds());
        if (refsError != null) {
            return refsError;
        }
        // 不忽略null
        this.updateById(AiBotConvert.INSTANCE.convert(form), false);
        insertBotMcpTools(form.getId(), form.getToolIds(), true);
        insertBotKnowledge(form.getId(), form.getKnowledgeIds(), true);
        return ResponseVO.ok();
    }

    /**
     * 统一校验挂载引用存在性（MCP 工具 / 知识库）；链式查询自动带租户过滤，跨部门引用直接拦截
     */
    private ResponseVO<Void> validateBotRefs(List<Long> toolIds, List<Long> knowledgeIds) {
        val invalidToolIds = filterNotExist(toolIds, AiMcpTool.class, AiMcpTool::getId);
        if (!invalidToolIds.isEmpty()) {
            return ResponseVO.recordNotFoundError("ToolIds", invalidToolIds);
        }
        val invalidKnowledgeIds = filterNotExist(knowledgeIds, AiKnowledge.class, AiKnowledge::getId);
        if (!invalidKnowledgeIds.isEmpty()) {
            return ResponseVO.recordNotFoundError("KnowledgeIds", invalidKnowledgeIds);
        }
        return null;
    }

    /**
     * 过滤出在指定实体表中不存在的 id（一次 in 查询，返回去重后的无效 id 列表）
     */
    private <T> List<Long> filterNotExist(List<Long> ids, Class<T> entityClass, LambdaGetter<T> idGetter) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        val existingIds = new HashSet<>(QueryChain.of(entityClass)
                .select(idGetter)
                .in(idGetter, ids)
                .listAs(Long.class));
        return ids.stream().distinct().filter(id -> !existingIds.contains(id)).toList();
    }

    private void insertBotMcpTools(Long botId, List<Long> toolIds, boolean isUpdate) {
        val botMcpToolList = toolIds.stream()
                .distinct()
                .map(toolId -> AiBotMcpTool.builder()
                        .botId(botId)
                        .toolId(toolId)
                        .build())
                .toList();

        if (isUpdate) {
            UpdateChain.of(AiBotMcpTool.class)
                    .eq(AiBotMcpTool::getBotId, botId)
                    .remove();
        }

        if (!botMcpToolList.isEmpty()) {
            botMcpToolMapper.insertBatch(botMcpToolList);
        }
    }

    private void insertBotKnowledge(Long botId, List<Long> knowledgeIds, boolean isUpdate) {
        val botKnowledgeList = knowledgeIds.stream()
                .distinct()
                .map(knowledgeId -> AiBotKnowledge.builder()
                        .botId(botId)
                        .knowledgeId(knowledgeId)
                        .build())
                .toList();

        if (isUpdate) {
            UpdateChain.of(AiBotKnowledge.class)
                    .eq(AiBotKnowledge::getBotId, botId)
                    .remove();
        }

        if (!botKnowledgeList.isEmpty()) {
            botKnowledgeMapper.insertBatch(botKnowledgeList);
        }
    }

    @Override
    public List<Long> getToolIds(Long botId) {
        return QueryChain.of(AiBotMcpTool.class)
                .select(AiBotMcpTool::getToolId)
                .eq(AiBotMcpTool::getBotId, botId)
                .listAs(Long.class);
    }

    @Override
    public List<Long> getKnowledgeIds(Long botId) {
        return QueryChain.of(AiBotKnowledge.class)
                .select(AiBotKnowledge::getKnowledgeId)
                .eq(AiBotKnowledge::getBotId, botId)
                .listAs(Long.class);
    }

    @Override
    public List<AiServiceTool> buildChatTools(Long botId) {
        val result = new ArrayList<AiServiceTool>();

        // MCP 工具部分：bot 未挂载/挂载失效时跳过，不影响后续知识库检索工具注册
        val toolIds = QueryChain.of(AiBotMcpTool.class)
                .select(AiBotMcpTool::getToolId)
                .eq(AiBotMcpTool::getBotId, botId)
                .listAs(Long.class);
        if (!CollectionUtils.isEmpty(toolIds)) {
            val mcpToolList = QueryChain.of(AiMcpTool.class)
                    .in(AiMcpTool::getId, toolIds)
                    .list();
            if (!CollectionUtils.isEmpty(mcpToolList)) {

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
        }

        appendKnowledgeSearchTool(botId, result);
        return result;
    }

    /**
     * 追加知识库检索工具（绑定了知识库的 bot 才注册；工具名与已注册 MCP 工具重名时放弃注册）
     */
    private void appendKnowledgeSearchTool(Long botId, List<AiServiceTool> result) {
        val knowledgeIds = getKnowledgeIds(botId);
        if (CollectionUtils.isEmpty(knowledgeIds)) {
            return;
        }
        val hasConflict = result.stream()
                .anyMatch(tool -> AiKnowledgeSearchToolBuilder.TOOL_NAME.equals(tool.toolSpecification().name()));
        if (hasConflict) {
            log.warn("知识库检索工具名与MCP工具重名，放弃注册, botId: {}", botId);
            return;
        }
        knowledgeSearchToolBuilder.build(knowledgeIds).ifPresent(result::add);
    }

    /**
     * 删除 bot 时级联清理两张挂载表（MCP 工具挂载原为孤儿数据，顺带修复）
     */
    @Override
    public boolean removeById(Serializable id) {
        UpdateChain.of(AiBotMcpTool.class)
                .eq(AiBotMcpTool::getBotId, id)
                .remove();
        UpdateChain.of(AiBotKnowledge.class)
                .eq(AiBotKnowledge::getBotId, id)
                .remove();
        return super.removeById(id);
    }

    /**
     * 临时建连执行工具调用，结果按 output_template 包装；失败返回错误文本交给 LLM 自行决策
     */
    private String executeMcpTool(String configJson, String outputTemplate, ToolExecutionRequest request) {
        try (McpClient client = buildClient(configJson)) {
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

    private McpClient buildClient(String configJson) {
        // 已保存的服务配置无需再校验，validate 仅用于反序列化为强类型配置
        val serverConfig = configValidator.validate(configJson);

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
                .initializationTimeout(TIMEOUT)
                .toolExecutionTimeout(TOOL_EXECUTION_TIMEOUT)
                .build();
    }
}
