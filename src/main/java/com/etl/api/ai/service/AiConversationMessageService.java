package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiConversationMessage;
import com.mybatisflex.core.service.IService;

import java.util.List;
import java.util.Map;

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
     * 回写最新 assistant 消息行的 toAttributes（tokenUsage + retrievalContent；
     * toBuilder 保留 text/thinking/toolExecutionRequests，仅替换 toAttributes）
     *
     * @param conversationId 会话id
     * @param attributes     toAttributes（见 {@link com.etl.api.ai.util.AiMessageUtil#toAttributes}）
     */
    void writeAssistantAttributes(Long conversationId, Map<String, Object> attributes);
}
