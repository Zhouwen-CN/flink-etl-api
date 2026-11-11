package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.entity.AiConversationMessage;
import com.etl.api.ai.mapper.AiConversationMessageMapper;
import com.etl.api.ai.service.AiConversationMessageService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI会话历史 服务层实现。
 *
 * @author chen
 * @since 2026-11-11
 */
@Service
@RequiredArgsConstructor
public class AiConversationMessageServiceImpl extends ServiceImpl<AiConversationMessageMapper, AiConversationMessage> implements AiConversationMessageService {

    @Override
    public List<AiConversationMessage> listByConversationId(Long conversationId) {
        return this.queryChain()
                .eq(AiConversationMessage::getConversationId, conversationId)
                .orderBy(AiConversationMessage::getId, true)
                .list();
    }

    @Override
    public void deleteByConversationId(Long conversationId) {
        this.updateChain()
                .eq(AiConversationMessage::getConversationId, conversationId)
                .remove();
    }
}
