package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.domain.form.AiMcpToolUpdateForm;
import com.etl.api.ai.domain.vo.AiMcpToolVO;
import com.etl.api.ai.mapper.AiMcpToolMapper;
import com.etl.api.ai.service.AiMcpToolService;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AI MCP工具表 服务层实现。
 *
 * @author chen
 * @since 2026-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiMcpToolServiceImpl extends ServiceImpl<AiMcpToolMapper, AiMcpTool> implements AiMcpToolService {

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
            return ResponseVO.recordNotFoundError(form.getId());
        }

        // 仅用户可配置字段；outputTemplate 传null表示清空（updateById 不忽略null，其余字段为已查出原值）
        entity.setOutputTemplate(form.getOutputTemplate());
        this.updateById(entity, false);
        log.info("更新MCP工具, toolId: {}, serverId: {}", entity.getId(), entity.getServerId());
        return ResponseVO.ok();
    }
}
