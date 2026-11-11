package com.etl.api.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;


public interface Assistant extends ChatMemoryAccess {
    /**
     * 聊天
     *
     * @param conversationId userId:conversationId
     * @param message        用户消息
     * @return response
     */
    String chat(@MemoryId Long conversationId, @UserMessage String message);

    TokenStream streamingChat(@MemoryId Long conversationId, @UserMessage String message);
}
