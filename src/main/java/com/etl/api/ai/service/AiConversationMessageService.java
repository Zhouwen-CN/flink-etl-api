package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiConversationMessage;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * AI会话历史 服务层。
 *
 * @author chen
 * @since 2026-11-11
 */
public interface AiConversationMessageService extends IService<AiConversationMessage> {

    /**
     * 按 conversationId 查询会话消息（按写入顺序）
     *
     * @param conversationId 会话id
     * @return 消息列表
     */
    List<AiConversationMessage> listByConversationId(Long conversationId);

    /**
     * 按 conversationId 删除会话消息
     *
     * @param conversationId 会话id
     */
    void deleteByConversationId(Long conversationId);

    /**
     * 回写最新 assistant 消息行的 message（含 attributes：tokenUsage + getRetrievalContentList，
     * 与 done 事件同一份 JSON；attributes 仅前端展示，不发给大模型）
     *
     * @param conversationId 会话id
     * @param messageJson    完整的 assistant 消息 JSON
     */
    void writeAssistantAttributes(Long conversationId, String messageJson);
}
