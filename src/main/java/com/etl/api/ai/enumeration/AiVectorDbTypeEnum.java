package com.etl.api.ai.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 向量数据库类型枚举（config_json 校验 + 工厂构建分发用）。
 *
 * <p>当前支持 Redis（Redis Stack，依赖 RediSearch + RedisJSON）与 Milvus。
 *
 * @author chen
 * @since 2026-11-18
 */
@Getter
public enum AiVectorDbTypeEnum {

    REDIS(1, "Redis"),
    MILVUS(2, "Milvus");

    private final int code;
    private final String desc;

    AiVectorDbTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * code 转枚举，未匹配返回 empty
     */
    public static Optional<AiVectorDbTypeEnum> of(Integer code) {
        if (code == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(e -> e.code == code)
                .findFirst();
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
