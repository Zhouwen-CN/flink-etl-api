package com.etl.api.ai.domain.dto;

import com.etl.api.ai.exception.AiVectorDbConfigException;
import org.springframework.util.StringUtils;

/**
 * 向量数据库连接配置契约（config_json 强类型 DTO 的公共接口）。
 *
 * <p>host/port 校验为所有类型公共规则，收敛到 {@link #validateCommon()}；
 * 各 DTO 实现 {@link #validate()} 时先调用公共校验，再追加类型特有规则
 * （如 Milvus 鉴权信息成对约束）。SDK 连接构建与集合命名不进本接口，仍收敛在工厂内。
 *
 * @author chen
 * @since 2026-11-20
 */
public interface AiVectorDbConfig {

    String getHost();

    Integer getPort();

    /**
     * 校验配置合法性，失败抛 {@link AiVectorDbConfigException}
     */
    void validate();

    /**
     * 公共校验：host 必填，port 为 1-65535 的整数
     */
    default void validateCommon() {
        if (!StringUtils.hasText(getHost())) {
            throw new AiVectorDbConfigException("向量数据库连接配置必须提供 host");
        }
        Integer port = getPort();
        if (port == null || port < 1 || port > 65535) {
            throw new AiVectorDbConfigException("向量数据库连接配置 port 必须为 1-65535 的整数");
        }
    }
}
