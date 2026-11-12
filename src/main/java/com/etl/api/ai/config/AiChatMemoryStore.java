package com.etl.api.ai.config;

import com.etl.api.ai.domain.entity.AiConversationMessage;
import com.etl.api.ai.service.AiConversationMessageService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于数据库的会话记忆存储，一行数据表示一条消息。
 *
 * @author chen
 * @since 2026-11-11
 */
@Component
@RequiredArgsConstructor
public class AiChatMemoryStore implements ChatMemoryStore {

    private final AiConversationMessageService aiConversationMessageService;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Long cid = toLong(memoryId);
        List<AiConversationMessage> records = aiConversationMessageService.listByConversationId(cid);
        List<ChatMessage> messages = new ArrayList<>(records.size());
        for (AiConversationMessage record : records) {
            messages.add(ChatMessageDeserializer.messageFromJson(record.getMessage()));
        }
        return messages;
    }

    @Override
    @Transactional
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // 增量更新：已保存消息与新消息前缀一致时只追加新消息；发生窗口淘汰（列表头部被移除）时全量重写
        Long cid = toLong(memoryId);
        List<AiConversationMessage> existing = aiConversationMessageService.listByConversationId(cid);

        int common = 0;
        int limit = Math.min(existing.size(), messages.size());
        for (int i = 0; i < limit; i++) {
            if (!existing.get(i).getMessage().equals(ChatMessageSerializer.messageToJson(messages.get(i)))) {
                break;
            }
            common++;
        }

        if (common < existing.size()) {
            aiConversationMessageService.deleteByConversationId(cid);
            aiConversationMessageService.saveBatch(toEntities(cid, messages));
            return;
        }

        if (common < messages.size()) {
            aiConversationMessageService.saveBatch(toEntities(cid, messages.subList(common, messages.size())));
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        aiConversationMessageService.deleteByConversationId(toLong(memoryId));
    }

    private Long toLong(Object conversationId) {
        if (conversationId instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(conversationId.toString());
    }

    private List<AiConversationMessage> toEntities(Long conversationId, List<ChatMessage> messages) {
        return messages.stream()
                .map(message -> AiConversationMessage.builder()
                        .conversationId(conversationId)
                        .message(ChatMessageSerializer.messageToJson(message))
                        .createdTime(LocalDateTime.now())
                        .build())
                .toList();
    }
}
