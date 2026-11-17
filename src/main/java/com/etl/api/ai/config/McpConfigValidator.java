package com.etl.api.ai.config;

import com.etl.api.ai.enumeration.McpTransportTypeEnum;
import com.etl.api.ai.exception.McpConfigException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * MCP config_json 校验器（Jackson 本地解析，不建连）。
 *
 * <p>规则：顶层 mcpServers 对象且恰好 1 个服务；stdio 字段拒绝；type 白名单；
 * url 必须为合法 http/https；catalogSource 目录引用拒绝。
 */
@Component
@RequiredArgsConstructor
public class McpConfigValidator {

    private static final String MCP_SERVERS_KEY = "mcpServers";
    private static final String CATALOG_SOURCE_KEY = "catalogSource";

    private final ObjectMapper objectMapper;

    /**
     * 校验 config_json 并返回单个服务的配置节点。
     *
     * @throws McpConfigException 校验失败（含具体原因）
     */
    public JsonNode validate(String configJson) {
        JsonNode root;
        try {
            root = objectMapper.readTree(configJson);
        } catch (Exception e) {
            throw new McpConfigException("MCP 配置JSON解析失败: " + e.getMessage());
        }

        if (root == null || !root.isObject()) {
            throw new McpConfigException("MCP 配置JSON必须为对象");
        }

        val servers = root.get(MCP_SERVERS_KEY);
        if (servers == null || !servers.isObject() || servers.isEmpty()) {
            throw new McpConfigException("MCP 配置必须包含非空的 mcpServers 对象");
        }
        if (servers.size() > 1) {
            throw new McpConfigException("一条记录仅支持配置一个 MCP 服务");
        }

        val server = servers.properties().iterator().next().getValue();
        if (server == null || !server.isObject()) {
            throw new McpConfigException("mcpServers 下的服务配置必须为对象");
        }

        if (server.has(CATALOG_SOURCE_KEY)) {
            throw new McpConfigException("不支持目录引用（catalogSource），目录功能后续迭代提供");
        }

        for (val stdioField : new String[]{"command", "args", "env", "cwd"}) {
            if (server.has(stdioField)) {
                throw new McpConfigException("不支持 stdio 本地进程配置（含 " + stdioField + " 字段），" +
                        "stdio 仅能通过管理员目录安装，目录功能后续迭代提供");
            }
        }

        val type = server.path("type").asText(null);
        if (!StringUtils.hasText(type)) {
            throw new McpConfigException("MCP 配置必须包含 type 字段（远程传输类型）");
        }
        if (!McpTransportTypeEnum.isRemoteSupported(type)) {
            throw new McpConfigException("不支持的传输类型: " + type +
                    "，仅允许: sse、http-sse、ssehttp、http、streamable、http-stream、streamablehttp");
        }

        val url = server.path("url").asText(null);
        if (!StringUtils.hasText(url)) {
            throw new McpConfigException("MCP 配置必须包含 url 字段");
        }
        validateUrl(url);

        val headers = server.get("headers");
        if (headers != null && !headers.isObject()) {
            throw new McpConfigException("headers 必须为对象");
        }

        return server;
    }

    private void validateUrl(String url) {
        try {
            val uri = new URI(url.trim());
            val scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new McpConfigException("url 仅支持 http/https 协议: " + url);
            }
        } catch (McpConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new McpConfigException("url 格式不合法: " + url);
        }
    }
}
