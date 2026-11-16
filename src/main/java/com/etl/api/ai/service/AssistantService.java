package com.etl.api.ai.service;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;

import java.util.List;


public interface AssistantService extends ChatMemoryAccess {

    /**
     * 聊天（流式）
     *
     * @param conversationId userId:conversationId
     * @param userMessage    用户消息文本
     * @param images         图片列表（base64 多模态内容，可为空）
     * @param params         配置参数
     * @return TokenStream
     */
    TokenStream streamingChat(@MemoryId Long conversationId, @UserMessage String userMessage, @UserMessage List<ImageContent> images, ChatRequestParameters params);
}
