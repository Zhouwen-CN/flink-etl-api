package com.etl.api.ai.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 知识库文档解析状态机：WAITING → PROCESSING → SUCCESS / FAILED。
 *
 * @author chen
 * @since 2026-11-20
 */
@Getter
@AllArgsConstructor
public enum AiDocParseStatusEnum {

    /**
     * 已入库待解析
     */
    WAITING("WAITING"),

    /**
     * 异步解析中
     */
    PROCESSING("PROCESSING"),

    /**
     * 解析并向量化成功
     */
    SUCCESS("SUCCESS"),

    /**
     * 解析或向量化失败
     */
    FAILED("FAILED");

    /**
     * 状态码（与 DB parse_status 存储值一致）
     */
    private final String code;
}
