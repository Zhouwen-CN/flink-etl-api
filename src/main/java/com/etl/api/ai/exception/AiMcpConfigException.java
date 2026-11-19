package com.etl.api.ai.exception;

import lombok.experimental.StandardException;

/**
 * MCP 配置异常（config_json 校验失败、工具同步失败等）
 */
@StandardException
public class AiMcpConfigException extends RuntimeException {
}
