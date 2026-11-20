package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.convert.AiMcpServerConvert;
import com.etl.api.ai.domain.entity.AiBotMcpTool;
import com.etl.api.ai.domain.entity.AiMcpServer;
import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.domain.form.AiMcpServerCreateForm;
import com.etl.api.ai.domain.form.AiMcpServerUpdateForm;
import com.etl.api.ai.domain.validator.AiMcpConfigValidator;
import com.etl.api.ai.domain.vo.AiMcpSelectorVO;
import com.etl.api.ai.domain.vo.AiMcpServerVO;
import com.etl.api.ai.mapper.AiMcpServerMapper;
import com.etl.api.ai.service.AiMcpServerService;
import com.etl.api.ai.service.AiMcpToolService;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AI MCP服务表 服务层实现。
 *
 * @author chen
 * @since 2026-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiMcpServerServiceImpl extends ServiceImpl<AiMcpServerMapper, AiMcpServer> implements AiMcpServerService {

    private final AiMcpConfigValidator configValidator;
    private final AiMcpToolService mcpToolService;

    @Override
    public PageVO<AiMcpServerVO> getPage(Integer currentPage, Integer pageSize, String searchName) {
        val page = this.queryChain()
                .like(AiMcpServer::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), AiMcpServerVO.class);
        return PageVO.from(page);
    }

    @Override
    public ResponseVO<Void> addAiMcpServer(AiMcpServerCreateForm form) {
        val exists = this.queryChain()
                .eq(AiMcpServer::getName, form.getName())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError("Name", form.getName());
        }

        // 本地格式校验（Jackson 解析，不建连）
        val serverConfig = configValidator.validate(form.getConfigJson());

        val entity = AiMcpServerConvert.INSTANCE.convert(form);
        this.save(entity);

        // 保存即同步工具：连接或拉取失败则整个保存失败（事务回滚）
        mcpToolService.syncTools(entity.getId(), serverConfig);
        log.info("新增MCP服务并同步工具, serverId: {}, name: {}", entity.getId(), entity.getName());
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> updateAiMcpServer(AiMcpServerUpdateForm form) {
        val old = this.getById(form.getId());
        if (old == null) {
            return ResponseVO.recordNotFoundError("ID", form.getId());
        }

        val nameExists = this.queryChain()
                .eq(AiMcpServer::getName, form.getName())
                .ne(AiMcpServer::getId, form.getId())
                .exists();
        if (nameExists) {
            return ResponseVO.recordExistsError("Name", form.getName());
        }

        // 前端每次修改都会回传旧值：先对比前后 config_json，发生变更才校验并重新同步工具
        val newConfigJson = form.getConfigJson();
        val oldConfigJson = old.getConfigJson();
        val configChanged = !Objects.equals(newConfigJson, oldConfigJson);
        if (configChanged) {
            // config_json 变更时重新同步工具，失败则整个更新失败、保持旧值（事务回滚）
            val serverConfig = configValidator.validate(newConfigJson);
            mcpToolService.syncTools(form.getId(), serverConfig);
        }

        val entity = AiMcpServerConvert.INSTANCE.convert(form);
        // 未变更时保持原值（避免规范化后的文本覆盖原始配置）
        entity.setConfigJson(configChanged ? newConfigJson : oldConfigJson);

        this.updateById(entity);
        log.info("更新MCP服务, serverId: {}, name: {}, configChanged: {}", entity.getId(), entity.getName(), configChanged);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeAiMcpServer(Long id) {
        this.removeById(id);

        // 物理删除该服务下的工具缓存，并级联清理挂载行（本迭代挂载表无数据，防御性清理）
        val mcpToolList = QueryChain.of(AiMcpTool.class)
                .eq(AiMcpTool::getServerId, id)
                .list();
        if (!CollectionUtils.isEmpty(mcpToolList)) {
            val toolIds = mcpToolList.stream().map(AiMcpTool::getId).toList();
            UpdateChain.of(AiBotMcpTool.class)
                    .in(AiBotMcpTool::getToolId, toolIds)
                    .remove();
            UpdateChain.of(AiMcpTool.class)
                    .eq(AiMcpTool::getServerId, id)
                    .remove();
            log.info("删除MCP服务, serverId: {}, removedToolCount: {}", id, mcpToolList.size());
        } else {
            log.info("删除MCP服务, serverId: {}", id);
        }
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> syncTools(Long id) {
        val server = this.getById(id);
        if (server == null) {
            return ResponseVO.recordNotFoundError("ID", id);
        }

        val serverConfig = configValidator.validate(server.getConfigJson());
        try {
            // 手动同步失败仅返回错误原因，不影响已有数据（不回滚，同步失败发生在建连/拉取阶段）
            mcpToolService.syncTools(id, serverConfig);
        } catch (Exception e) {
            log.warn("手动同步MCP工具失败, serverId: {}, error: {}", id, e.getMessage());
            throw e;
        }
        return ResponseVO.ok();
    }

    @Override
    public List<AiMcpSelectorVO> selector() {
        val servers = this.queryChain()
                .orderBy(AiMcpServer::getId, true)
                .list();
        val toolsByServerId = QueryChain.of(AiMcpTool.class)
                .orderBy(AiMcpTool::getId, true)
                .list()
                .stream()
                .collect(Collectors.groupingBy(AiMcpTool::getServerId));

        return servers.stream().map(server -> {
            val serverNode = new AiMcpSelectorVO();
            // 服务节点仅作分组：value 加前缀避免与工具id冲突，点击即全选其下所有工具
            serverNode.setValue("server-" + server.getId());
            serverNode.setLabel(server.getName());

            val toolNodes = toolsByServerId.getOrDefault(server.getId(), List.of()).stream().map(tool -> {
                val toolNode = new AiMcpSelectorVO();
                toolNode.setValue(tool.getId());
                toolNode.setLabel(tool.getName());
                return toolNode;
            }).toList();
            serverNode.setChildren(toolNodes);
            return serverNode;
        }).toList();
    }
}
