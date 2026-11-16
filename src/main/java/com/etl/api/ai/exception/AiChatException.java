package com.etl.api.ai.exception;

import lombok.experimental.StandardException;

/**
 * AI 聊天业务异常（Bot/模型不存在、Bot 不支持图片、会话不存在等）
 */
@StandardException
public class AiChatException extends RuntimeException {
}
