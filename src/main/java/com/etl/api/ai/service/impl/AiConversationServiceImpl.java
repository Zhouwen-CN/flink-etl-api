package com.etl.api.ai.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.etl.api.ai.config.AiChatModelFactory;
import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.entity.AiConversation;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.exception.AiChatException;
import com.etl.api.ai.mapper.AiConversationMapper;
import com.etl.api.ai.service.AiBotService;
import com.etl.api.ai.service.AiConversationMessageService;
import com.etl.api.ai.service.AiConversationService;
import com.etl.api.ai.service.AiModelService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * AI会话 服务层实现。
 *
 * @author chen
 * @since 2026-11-12
 */
@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl extends ServiceImpl<AiConversationMapper, AiConversation> implements AiConversationService {

    private static final String TITLE_SYSTEM_PROMPT = """
            你是一个会话标题生成助手。请根据用户发送的第一条消息，生成一个简洁、准确的会话标题。
            要求：
            1. 不超过20个汉字；
            2. 只返回标题文本本身，不要添加任何解释、前后引号或标点符号；
            3. 准确概括用户的核心意图。
            """;

    private static final String DEFAULT_TITLE = "新会话";

    private final AiConversationMessageService conversationMessageService;
    private final ChatModel chatModel;

    @Override
    public AiConversation createConversation(Long botId, String message) {
        String title = generateTitle(chatModel, message);
        AiConversation conversation = AiConversation.builder()
                .userId(StpUtil.getLoginIdAsLong())
                .botId(botId)
                .title(title)
                .build();
        this.save(conversation);
        return conversation;
    }

    @Override
    public void deleteConversation(Long id) {
        AiConversation conversation = this.getById(id);
        if (conversation == null || !Objects.equals(conversation.getUserId(), StpUtil.getLoginIdAsLong())) {
            throw new AiChatException("会话不存在: " + id);
        }
        conversationMessageService.deleteByConversationId(id);
        this.removeById(id);
    }

    @Override
    public void renameConversation(Long id, String title) {
        AiConversation conversation = this.getById(id);
        if (conversation == null || !Objects.equals(conversation.getUserId(), StpUtil.getLoginIdAsLong())) {
            throw new AiChatException("会话不存在: " + id);
        }
        String trimmed = title.trim();
        conversation.setTitle(trimmed.isEmpty() ? DEFAULT_TITLE : trimmed);
        this.updateById(conversation);
    }

    private String generateTitle(ChatModel chatModel, String userMessage) {
        ChatResponse response = chatModel.chat(List.of(
                SystemMessage.from(TITLE_SYSTEM_PROMPT),
                UserMessage.from(userMessage)
        ));
        String title = response.aiMessage().text();
        if (title == null) {
            return DEFAULT_TITLE;
        }
        // 清理模型可能返回的前后引号/空白
        title = title.trim().replaceAll("^[\"'「」『』《》]+|[\"'「」『』《》]+$", "");
        // 数据库字段 varchar(255)，兜底截断
        if (title.length() > 255) {
            title = title.substring(0, 255);
        }
        if (title.isEmpty()) {
            title = DEFAULT_TITLE;
        }
        return title;
    }
}
