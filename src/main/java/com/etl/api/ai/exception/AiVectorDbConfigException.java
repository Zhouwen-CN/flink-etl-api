package com.etl.api.ai.exception;

import lombok.experimental.StandardException;

/**
 * 向量数据库配置异常（config_json 校验失败、连接验证失败等）
 */
@StandardException
public class AiVectorDbConfigException extends RuntimeException {
}
