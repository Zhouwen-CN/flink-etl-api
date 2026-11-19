package com.etl.api.ai.domain.validator;

import com.etl.api.ai.domain.dto.MilvusConfigDTO;
import com.etl.api.ai.domain.dto.RedisConfigDTO;
import com.etl.api.ai.enumeration.AiVectorDbTypeEnum;
import com.etl.api.ai.exception.AiVectorDbConfigException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 向量数据库 config_json 校验器（Jackson 本地解析，不建连）。
 *
 * <p>按 type 分发到对应强类型 DTO：反序列化时开启未知字段拒绝，
 * 缺失字段 / 互斥冲突抛 {@link AiVectorDbConfigException}。
 * 后续扩展其他向量库类型时新增 DTO + 校验分支即可。
 *
 * @author chen
 * @since 2026-11-18
 */
@Component
@RequiredArgsConstructor
public class AiVectorDbConfigValidator {

    private final ObjectMapper objectMapper;

    /**
     * 校验 config_json 并返回强类型配置（REDIS -> {@link RedisConfigDTO}，MILVUS -> {@link MilvusConfigDTO}）。
     *
     * @throws AiVectorDbConfigException 校验失败（含具体原因）
     */
    public Object validate(Integer type, String configJson) {
        val dbType = validateType(type);
        return switch (dbType) {
            case REDIS -> validateRedis(configJson);
            case MILVUS -> validateMilvus(configJson);
        };
    }

    private AiVectorDbTypeEnum validateType(Integer type) {
        if (type == null) {
            throw new AiVectorDbConfigException("向量数据库配置必须包含 type 字段");
        }
        return AiVectorDbTypeEnum.of(type)
                .orElseThrow(() -> new AiVectorDbConfigException(
                        "不支持的向量数据库类型: " + type + "，仅允许: 1-redis、2-milvus"));
    }

    /**
     * 严格反序列化：未知字段直接报错
     */
    private <T> T parse(String configJson, Class<T> clazz, String name) {
        try {
            return objectMapper.readValue(configJson, clazz);
        } catch (Exception e) {
            throw new AiVectorDbConfigException(name + " 连接配置JSON解析失败: " + e.getMessage());
        }
    }

    private RedisConfigDTO validateRedis(String configJson) {
        if (!StringUtils.hasText(configJson)) {
            throw new AiVectorDbConfigException("Redis 连接配置JSON不能为空");
        }
        val config = parse(configJson, RedisConfigDTO.class, "Redis");

        if (!StringUtils.hasText(config.getHost())) {
            throw new AiVectorDbConfigException("Redis 连接配置必须提供 host");
        }
        // host 模式：port 必填且合法
        val port = config.getPort();
        if (port == null || port < 1 || port > 65535) {
            throw new AiVectorDbConfigException("Redis 连接配置 port 必须为 1-65535 的整数");
        }
        return config;
    }

    private MilvusConfigDTO validateMilvus(String configJson) {
        if (!StringUtils.hasText(configJson)) {
            throw new AiVectorDbConfigException("Milvus 连接配置JSON不能为空");
        }
        val config = parse(configJson, MilvusConfigDTO.class, "Milvus");

        if (!StringUtils.hasText(config.getHost())) {
            throw new AiVectorDbConfigException("Milvus 连接配置必须提供 host");
        }
        val port = config.getPort();
        if (port == null || port < 1 || port > 65535) {
            throw new AiVectorDbConfigException("Milvus 连接配置 port 必须为 1-65535 的整数");
        }
        // 鉴权信息必须成对出现（Milvus 开启鉴权时 username/password 同时必填）
        val hasUsername = StringUtils.hasText(config.getUsername());
        val hasPassword = StringUtils.hasText(config.getPassword());
        if (hasUsername != hasPassword) {
            throw new AiVectorDbConfigException("Milvus 连接配置 username 与 password 必须成对提供");
        }
        return config;
    }
}
