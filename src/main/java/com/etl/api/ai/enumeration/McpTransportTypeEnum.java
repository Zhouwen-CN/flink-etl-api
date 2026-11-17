package com.etl.api.ai.enumeration;

import lombok.Getter;
import lombok.val;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * MCP 传输类型枚举（config_json 校验 + transport 构建分发用）。
 *
 * <p>本迭代远程传输统一使用 {@code StreamableHttpMcpTransport}（自动探测 SSE / Streamable HTTP 协议）。
 * stdio 类型仅限管理员目录（后续迭代），前端提交直接拒绝。
 */
@Getter
public enum McpTransportTypeEnum {

    SSE("SSE", Set.of("sse", "http-sse", "ssehttp")),
    STREAMABLE_HTTP("Streamable HTTP", Set.of("http", "streamable", "http-stream", "streamablehttp")),
    STDIO("stdio", Set.of("stdio"));

    private final String desc;
    private final Set<String> typeAliases;

    McpTransportTypeEnum(String desc, Set<String> typeAliases) {
        this.desc = desc;
        this.typeAliases = typeAliases;
    }

    /**
     * type 字符串转枚举，未匹配返回 empty
     */
    public static Optional<McpTransportTypeEnum> of(String type) {
        if (type == null) {
            return Optional.empty();
        }
        val lower = type.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(e -> e.typeAliases.contains(lower))
                .findFirst();
    }

    /**
     * type 是否在允许的远程传输白名单内（stdio 不在白名单，仅限目录）
     */
    public static boolean isRemoteSupported(String type) {
        return of(type).filter(e -> e != STDIO).isPresent();
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
