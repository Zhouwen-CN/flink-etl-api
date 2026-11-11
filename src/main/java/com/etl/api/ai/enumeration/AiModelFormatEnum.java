package com.etl.api.ai.enumeration;

import lombok.Getter;

/**
 * AI模型接口格式枚举
 */
@Getter
public enum AiModelFormatEnum {

    OPENAI_CHAT_COMPLETIONS(1, "OpenAI Chat Completions"),
    OPENAI_RESPONSES(2, "OpenAI Responses"),
    ANTHROPIC_MESSAGES(3, "Anthropic Messages");

    private final int code;
    private final String desc;

    AiModelFormatEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * code 转枚举，null 默认 OpenAI Chat Completions，未知 code 抛出异常
     */
    public static AiModelFormatEnum of(Integer code) {
        if (code == null) {
            return OPENAI_CHAT_COMPLETIONS;
        }
        for (AiModelFormatEnum format : values()) {
            if (format.code == code) {
                return format;
            }
        }
        throw new IllegalArgumentException("未知的 AI 模型接口格式: " + code);
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
