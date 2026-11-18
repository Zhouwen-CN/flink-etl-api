package com.etl.api.ai.enumeration;

import lombok.Getter;

/**
 * AI模型类型枚举
 */
@Getter
public enum AiModelTypeEnum {

    CHAT(1, "Chat"),
    EMBEDDING(2, "Embedding");

    private final int code;
    private final String desc;

    AiModelTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * code 转枚举，null 默认 Chat，未知 code 抛出异常
     */
    public static AiModelTypeEnum of(Integer code) {
        for (AiModelTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的 AI 模型类型: " + code);
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
