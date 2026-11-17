package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.domain.form.AiMcpToolUpdateForm;
import com.etl.api.ai.domain.vo.AiMcpToolVO;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * AI MCP工具表 服务层（服务端工具清单缓存）。
 *
 * @author chen
 * @since 2026-11-17
 */
public interface AiMcpToolService extends IService<AiMcpTool> {

    /**
     * 分页查询指定服务的工具清单
     */
    PageVO<AiMcpToolVO> getPage(Integer currentPage, Integer pageSize, Long serverId, String searchName);

    /**
     * 更新工具用户可配置字段（outputTemplate，传null表示清空）
     */
    ResponseVO<Void> updateAiMcpTool(AiMcpToolUpdateForm form);
}
