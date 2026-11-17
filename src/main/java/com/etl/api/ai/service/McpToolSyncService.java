package com.etl.api.ai.service;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * MCP 工具同步服务：临时连接 listTools + upsert/失效删除级联（用后即关，不驻留客户端）。
 *
 * <p>触发时机：新增保存、编辑且 config_json 变更、手动同步接口（PRD F1.6/F1.7）。
 */
public interface McpToolSyncService {

    /**
     * 同步指定 MCP 服务的工具清单到 T_AI_MCP_TOOL 缓存表。
     *
     * <p>upsert 按 server_id + name 匹配：新工具插入；已存在的只覆盖同步类字段
     * （description / input_schema），不覆盖用户配置的 output_template；
     * 服务端已消失的工具物理删除并级联删除挂载行。
     *
     * @param serverId     mcp服务id
     * @param serverConfig 单个服务的配置节点（已经过 McpConfigValidator 校验）
     * @throws com.etl.api.ai.exception.McpConfigException 建连或拉取工具清单失败
     */
    void syncTools(Long serverId, JsonNode serverConfig);
}
