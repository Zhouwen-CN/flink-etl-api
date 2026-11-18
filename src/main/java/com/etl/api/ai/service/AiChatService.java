package com.etl.api.ai.service;

import com.etl.api.ai.domain.form.AiChatForm;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI聊天 服务层。
 *
 * @author chen
 * @since 2026-11-18
 */
public interface AiChatService {

    /**
     * 聊天（SSE 流式）：校验 Bot/模型状态 → 构建 AiService 与挂载的 MCP 工具 →
     * TokenStream 推送增量/工具事件至 SSE，客户端断开或超时时取消生成。
     *
     * @param form 聊天表单
     * @return SSE emitter
     */
    SseEmitter streamChat(AiChatForm form);
}
