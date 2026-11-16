package com.etl.api.ai.service;

import dev.langchain4j.data.message.Content;
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
     * @param conversationId 会话ID（作为聊天记忆ID）
     * @param contents       用户消息内容列表（文本 + 多模态图片）
     * @param params         配置参数
     * @return TokenStream
     */
    TokenStream streamingChat(@MemoryId Long conversationId, @UserMessage List<Content> contents, ChatRequestParameters params);
}
