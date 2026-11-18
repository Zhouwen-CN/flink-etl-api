package com.etl.api.ai.domain.dto;

import com.etl.api.ai.enumeration.AiMcpTransportTypeEnum;
import lombok.Data;

import java.util.Map;

/**
 * MCP 服务连接配置（config_json 平铺格式的强类型映射）。
 *
 * <p>一条配置仅对应一个远程服务；stdio 本地进程字段与目录引用（catalogSource）
 * 属于未知字段，校验器开启 FAIL_ON_UNKNOWN_PROPERTIES 后在反序列化阶段即被拒绝。
 *
 * @author chen
 * @since 2026-11-18
 */
@Data
public class McpServerConfigDTO {

    /**
     * 传输类型，白名单见 {@link AiMcpTransportTypeEnum}
     */
    private String type;

    /**
     * 服务地址，仅允许 http/https
     */
    private String url;

    /**
     * 自定义请求头，可选
     */
    private Map<String, String> headers;
}
