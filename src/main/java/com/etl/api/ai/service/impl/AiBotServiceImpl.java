package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.convert.AiBotConvert;
import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.entity.AiBotKnowledge;
import com.etl.api.ai.domain.entity.AiBotMcpTool;
import com.etl.api.ai.domain.entity.AiKnowledge;
import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.domain.form.AiBotCreateForm;
import com.etl.api.ai.domain.form.AiBotUpdateForm;
import com.etl.api.ai.mapper.AiBotKnowledgeMapper;
import com.etl.api.ai.mapper.AiBotMapper;
import com.etl.api.ai.mapper.AiBotMcpToolMapper;
import com.etl.api.ai.service.AiBotService;
import com.etl.api.ai.service.manager.AiChatToolManager;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.LambdaGetter;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AI Bot表 服务层实现。
 *
 * <p>负责 bot CRUD 与挂载表维护；聊天工具组装见
 * {@link AiChatToolManager}。
 *
 * @author chen
 * @since 2026-11-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiBotServiceImpl extends ServiceImpl<AiBotMapper, AiBot> implements AiBotService {

    /**
     * 挂载表批量插入无链式 API（insertBatch），保留 mapper
     */
    private final AiBotMcpToolMapper botMcpToolMapper;
    private final AiBotKnowledgeMapper botKnowledgeMapper;

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
    private ResponseVO<Void> validateBotRefs(Set<Long> toolIds, Set<Long> knowledgeIds) {
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
     * 过滤出在指定实体表中不存在的 id（一次 in 查询，入参为 Set 已天然去重）
     */
    private <T> List<Long> filterNotExist(Set<Long> ids, Class<T> entityClass, LambdaGetter<T> idGetter) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        val existingIds = new HashSet<>(QueryChain.of(entityClass)
                .select(idGetter)
                .in(idGetter, ids)
                .listAs(Long.class));
        return ids.stream().filter(id -> !existingIds.contains(id)).toList();
    }

    private void insertBotMcpTools(Long botId, Set<Long> toolIds, boolean isUpdate) {
        val botMcpToolList = toolIds.stream()
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

    private void insertBotKnowledge(Long botId, Set<Long> knowledgeIds, boolean isUpdate) {
        val botKnowledgeList = knowledgeIds.stream()
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
}
