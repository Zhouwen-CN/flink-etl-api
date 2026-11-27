package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.entity.AiConversationMessage;
import com.etl.api.ai.mapper.AiConversationMessageMapper;
import com.etl.api.ai.service.AiConversationMessageService;
import com.etl.api.ai.util.AiMessageUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * AI会话历史 服务层实现。
 *
 * @author chen
 * @since 2026-11-11
 */
@Slf4j
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

    @Override
    public void writeAssistantAttributes(Long conversationId, Map<String, Object> attributes) {
        // onCompleteResponse 时最终 assistant 行必已落库（LC4j addToMemory 先于回调），为最新行；
        // 仍按消息类型定位兜底，规避工具往返中间行的时序意外
        List<AiConversationMessage> records = this.queryChain()
                .eq(AiConversationMessage::getConversationId, conversationId)
                .orderBy(AiConversationMessage::getId, false)
                .list();
        for (AiConversationMessage record : records) {
            ChatMessage chatMessage = ChatMessageDeserializer.messageFromJson(record.getMessage());
            if (chatMessage instanceof AiMessage aiMessage) {
                this.updateChain()
                        .set(AiConversationMessage::getMessage, AiMessageUtil.mergeAttributes(aiMessage, attributes))
                        .eq(AiConversationMessage::getId, record.getId())
                        .update();
                return;
            }
        }
        log.warn("未定位到 assistant 消息行, conversationId={}", conversationId);
    }
}
