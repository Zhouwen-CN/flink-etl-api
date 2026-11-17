package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.convert.AiBotConvert;
import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.entity.AiBotMcpTool;
import com.etl.api.ai.domain.form.AiBotCreateForm;
import com.etl.api.ai.domain.form.AiBotUpdateForm;
import com.etl.api.ai.mapper.AiBotMapper;
import com.etl.api.ai.mapper.AiBotMcpToolMapper;
import com.etl.api.ai.service.AiBotService;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI Bot表 服务层实现。
 *
 * @author chen
 * @since 2026-11-13
 */
@Service
@RequiredArgsConstructor
public class AiBotServiceImpl extends ServiceImpl<AiBotMapper, AiBot> implements AiBotService {

    private final AiBotMcpToolMapper botMcpToolMapper;

    @Override
    public ResponseVO<Void> addAiBot(AiBotCreateForm form) {
        val exists = this.queryChain()
                .eq(AiBot::getName, form.getName())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(form.getName());
        }
        val entity = AiBotConvert.INSTANCE.convert(form);
        this.save(entity);
        saveBotMcpTools(entity.getId(), form.getToolIds(), false);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> updateAiBot(AiBotUpdateForm form) {
        val exists = this.queryChain()
                .eq(AiBot::getName, form.getName())
                .ne(AiBot::getId, form.getId())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(form.getName());
        }
        // 不忽略null
        this.updateById(AiBotConvert.INSTANCE.convert(form), false);
        saveBotMcpTools(form.getId(), form.getToolIds(), true);
        return ResponseVO.ok();
    }

    private void saveBotMcpTools(Long botId, List<Long> toolIds, boolean isUpdate) {
        val botMcpToolList = toolIds.stream()
                .distinct()
                .map(toolId -> AiBotMcpTool.builder()
                        .botId(botId)
                        .toolId(toolId)
                        .build())
                .toList();

        if (isUpdate) {
            botMcpToolMapper.deleteByQuery(
                    QueryWrapper.create()
                            .eq(AiBotMcpTool::getBotId, botId)
            );
        }

        botMcpToolMapper.insertBatch(botMcpToolList);
    }

    @Override
    public List<Long> getToolIds(Long botId) {
        return botMcpToolMapper.selectListByQueryAs(
                QueryWrapper.create()
                        .select(AiBotMcpTool::getToolId)
                        .eq(AiBotMcpTool::getBotId, botId),
                Long.class
        );
    }
}
