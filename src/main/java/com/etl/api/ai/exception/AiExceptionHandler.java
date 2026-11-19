package com.etl.api.ai.exception;

import com.etl.api.sys.domain.vo.ResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class AiExceptionHandler {

    /**
     * AI 聊天业务异常
     */
    @ExceptionHandler(AiChatException.class)
    public ResponseVO<Void> aiChatExceptionHandler(AiChatException e) {
        return ResponseVO.error(e.getMessage());
    }

    /**
     * MCP 配置异常（config_json 校验失败、工具同步失败）
     */
    @ExceptionHandler(AiMcpConfigException.class)
    public ResponseVO<Void> mcpConfigExceptionHandler(AiMcpConfigException e) {
        return ResponseVO.error(e.getMessage());
    }

    /**
     * 向量数据库配置异常（config_json 校验失败、连接验证失败）
     */
    @ExceptionHandler(AiVectorDbConfigException.class)
    public ResponseVO<Void> vectorDbConfigExceptionHandler(AiVectorDbConfigException e) {
        return ResponseVO.error(e.getMessage());
    }
}
