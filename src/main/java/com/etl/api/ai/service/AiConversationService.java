package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiConversation;
import com.mybatisflex.core.service.IService;

/**
 * AI会话 服务层。
 *
 * @author chen
 * @since 2026-11-12
 */
public interface AiConversationService extends IService<AiConversation> {

    /**
     * 创建会话：调用模型将用户首条消息总结为标题，插入 T_AI_CONVERSATION 表。
     *
     * @param modelId 模型ID
     * @param message 用户首条消息
     * @return 会话实体（含生成的主键 id 与 title）
     */
    AiConversation createConversation(Long modelId, String message);

    /**
     * 删除会话及其对应的会话消息（校验会话归属当前用户）。
     *
     * @param id 会话id
     */
    void deleteConversation(Long id);

    /**
     * 会话重命名（校验会话归属当前用户）。
     *
     * @param id    会话id
     * @param title 新标题
     */
    void renameConversation(Long id, String title);
}
