package com.etl.api.ai.domain.validator;

import com.etl.api.ai.domain.dto.AiVectorDbConfig;
import com.etl.api.ai.domain.dto.MilvusConfigDTO;
import com.etl.api.ai.domain.dto.RedisConfigDTO;
import com.etl.api.ai.enumeration.AiVectorDbTypeEnum;
import com.etl.api.ai.exception.AiVectorDbConfigException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.etl.api.ai.enumeration.AiVectorDbTypeEnum.MILVUS;
import static com.etl.api.ai.enumeration.AiVectorDbTypeEnum.REDIS;

/**
 * 向量数据库 config_json 校验器（Jackson 本地解析，不建连）。
 *
 * <p>按 type 反序列化为对应强类型 DTO（映射收敛在 {@link AiVectorDbTypeEnum#dtoClass()}），
 * 反序列化时开启未知字段拒绝，字段规则收敛在各 DTO 的 {@code validate()}
 * （公共 host/port 规则在 {@link AiVectorDbConfig#validateCommon()}）。
 * 后续扩展其他向量库类型时：新增 DTO 实现 AiVectorDbConfig + 枚举项登记 dtoClass 即可。
 *
 * @author chen
 * @since 2026-11-18
 */
@Component
@RequiredArgsConstructor
public class AiVectorDbConfigValidator {

    private final ObjectMapper objectMapper;

    /**
     * 校验 config_json 并返回强类型配置。
     *
     * @throws AiVectorDbConfigException 校验失败（含具体原因）
     */
    public AiVectorDbConfig validate(Integer type, String configJson) {
        val dbType = requireSupportedType(type);
        if (!StringUtils.hasText(configJson)) {
            throw new AiVectorDbConfigException(dbType + " 连接配置JSON不能为空");
        }
        val config = parse(configJson, this.dtoClass(dbType), dbType.toString());
        config.validate();
        return config;
    }

    private AiVectorDbTypeEnum requireSupportedType(Integer type) {
        if (type == null) {
            throw new AiVectorDbConfigException("向量数据库配置必须包含 type 字段");
        }
        return AiVectorDbTypeEnum.of(type)
                .orElseThrow(() -> new AiVectorDbConfigException(
                        "不支持的向量数据库类型: " + type + "，仅允许: 1-redis、2-milvus"));
    }

    /**
     * 类型对应的 config_json 强类型 DTO（type -> DTO 映射唯一收敛点）
     */
    private Class<? extends AiVectorDbConfig> dtoClass(AiVectorDbTypeEnum dbType) {
        return switch (dbType) {
            case REDIS -> RedisConfigDTO.class;
            case MILVUS -> MilvusConfigDTO.class;
        };
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
}
