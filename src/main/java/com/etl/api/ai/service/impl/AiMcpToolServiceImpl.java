package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.dto.AiMcpServerConfigDTO;
import com.etl.api.ai.domain.entity.AiBotMcpTool;
import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.domain.form.AiMcpToolUpdateForm;
import com.etl.api.ai.domain.vo.AiMcpToolVO;
import com.etl.api.ai.mapper.AiMcpToolMapper;
import com.etl.api.ai.service.AiMcpToolService;
import com.etl.api.ai.service.manager.AiMcpToolManager;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.agent.tool.ToolSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI MCP工具表 服务层实现。
 *
 * <p>工具目录同步：临时建连与工具清单拉取统一走 {@link AiMcpToolManager#fetchTools}，
 * 本类仅负责同步结果的持久化（新增/更新/清理失效工具及挂载行）。
 *
 * @author chen
 * @since 2026-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiMcpToolServiceImpl extends ServiceImpl<AiMcpToolMapper, AiMcpTool> implements AiMcpToolService {

    private final AiMcpToolManager mcpToolManager;

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
    public void syncTools(Long serverId, AiMcpServerConfigDTO serverConfig) {
        List<ToolSpecification> specs = mcpToolManager.fetchTools(serverConfig);

        val existing = this.queryChain()
                .eq(AiMcpTool::getServerId, serverId)
                .list()
                .stream()
                .collect(HashMap<String, AiMcpTool>::new, (m, t) -> m.put(t.getName(), t), Map::putAll);

        for (val spec : specs) {
            val description = spec.description();
            val inputSchema = spec.toJson();
            val old = existing.remove(spec.name());
            if (old == null) {
                this.save(AiMcpTool.builder()
                        .serverId(serverId)
                        .name(spec.name())
                        .description(description)
                        .inputSchema(inputSchema)
                        .build());
            } else {
                // 只覆盖同步类字段，不得覆盖用户配置的 output_template
                UpdateChain.of(AiMcpTool.class)
                        .set(AiMcpTool::getDescription, description)
                        .set(AiMcpTool::getInputSchema, inputSchema)
                        .eq(AiMcpTool::getId, old.getId())
                        .update();
            }
        }

        // 服务端已消失的工具物理删除，并级联删除挂载行
        if (!existing.isEmpty()) {
            val staleIds = existing.values().stream().map(AiMcpTool::getId).toList();
            UpdateChain.of(AiBotMcpTool.class)
                    .in(AiBotMcpTool::getToolId, staleIds)
                    .remove();
            UpdateChain.of(AiMcpTool.class)
                    .in(AiMcpTool::getId, staleIds)
                    .remove();
            log.info("MCP工具同步清理失效工具, serverId: {}, removedTools: {}", serverId, existing.keySet());
        }

        log.info("MCP工具同步完成, serverId: {}, toolCount: {}", serverId, specs.size());
    }
}
