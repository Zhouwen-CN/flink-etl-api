package com.etl.api.ai.service;

import com.etl.api.ai.domain.dto.AiMcpServerConfigDTO;
import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.domain.form.AiMcpToolUpdateForm;
import com.etl.api.ai.domain.vo.AiMcpToolVO;
import com.etl.api.ai.exception.AiMcpConfigException;
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

    /**
     * 同步指定 MCP 服务的工具清单到 T_AI_MCP_TOOL 缓存表（临时连接 listTools，用后即关）。
     *
     * <p>upsert 按 server_id + name 匹配：新工具插入；已存在的只覆盖同步类字段
     * （description / input_schema），不覆盖用户配置的 output_template；
     * 服务端已消失的工具物理删除并级联删除挂载行。
     *
     * @param serverId     mcp服务id
     * @param serverConfig 单个服务的连接配置（已经过 McpConfigValidator 校验）
     * @throws AiMcpConfigException 建连或拉取工具清单失败
     */
    void syncTools(Long serverId, AiMcpServerConfigDTO serverConfig);
}
