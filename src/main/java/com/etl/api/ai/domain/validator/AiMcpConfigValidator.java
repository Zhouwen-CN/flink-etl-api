package com.etl.api.ai.domain.validator;

import com.etl.api.ai.domain.dto.AiMcpServerConfigDTO;
import com.etl.api.ai.enumeration.AiMcpTransportTypeEnum;
import com.etl.api.ai.exception.AiMcpConfigException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * MCP config_json 校验器（Jackson 本地解析，不建连）。
 *
 * <p>config_json 为单个服务的平铺格式（type / url / headers），反序列化为
 * {@link AiMcpServerConfigDTO} 时开启未知字段拒绝（stdio 字段、catalogSource 等一律报错）；
 * type 白名单限定远程传输；url 必须为合法 http/https。
 */
@Component
@RequiredArgsConstructor
public class AiMcpConfigValidator {

    private final ObjectMapper objectMapper;

    /**
     * 校验 config_json 并返回强类型配置。
     *
     * @throws AiMcpConfigException 校验失败（含具体原因）
     */
    public AiMcpServerConfigDTO validate(String configJson) {
        val config = parse(configJson);
        validateType(config.getType());
        validateUrl(config.getUrl());
        return config;
    }

    /**
     * 严格反序列化：未知字段（stdio 字段、catalogSource 等）直接报错
     */
    private AiMcpServerConfigDTO parse(String configJson) {
        try {
            return objectMapper.readValue(configJson, AiMcpServerConfigDTO.class);
        } catch (Exception e) {
            throw new AiMcpConfigException("MCP 配置JSON解析失败: " + e.getMessage());
        }
    }

    private void validateType(String type) {
        if (!StringUtils.hasText(type)) {
            throw new AiMcpConfigException("MCP 配置必须包含 type 字段（远程传输类型）");
        }
        if (!AiMcpTransportTypeEnum.isRemoteSupported(type)) {
            throw new AiMcpConfigException("不支持的传输类型: " + type +
                    "，仅允许: sse、http-sse、ssehttp、http、streamable、http-stream、streamablehttp");
        }
    }

    private void validateUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new AiMcpConfigException("MCP 配置必须包含 url 字段");
        }
        try {
            val uri = new URI(url.trim());
            val scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new AiMcpConfigException("url 仅支持 http/https 协议: " + url);
            }
        } catch (AiMcpConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new AiMcpConfigException("url 格式不合法: " + url);
        }
    }
}
