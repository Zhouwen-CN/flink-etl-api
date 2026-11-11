package com.etl.api.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;


public interface Assistant extends ChatMemoryAccess {
    /**
     * 聊天
     *
     * @param memoryId userId:memoryId
     * @param message  用户消息
     * @return response
     */
    String chat(@MemoryId String memoryId, @UserMessage String message);
}
