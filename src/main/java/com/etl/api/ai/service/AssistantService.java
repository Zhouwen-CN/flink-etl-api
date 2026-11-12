package com.etl.api.ai.service;

import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;


public interface AssistantService extends ChatMemoryAccess {

    /**
     * 聊天（流式）
     *
     * @param conversationId userId:conversationId
     * @param message        用户消息
     * @param params         配置参数
     * @return TokenStream
     */
    TokenStream streamingChat(@MemoryId Long conversationId, @UserMessage String message, ChatRequestParameters params);
}
