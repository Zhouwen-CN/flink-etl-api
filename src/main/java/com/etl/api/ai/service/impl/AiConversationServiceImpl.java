package com.etl.api.ai.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.etl.api.ai.config.ChatModelFactory;
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
import org.springframework.beans.factory.annotation.Value;
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


    @Value("${custom.ai.title-system-prompt}")
    private String titleSystemPrompt;

    private static final String DEFAULT_TITLE = "新会话";

    private final AiBotService botService;
    private final AiModelService modelService;
    private final ChatModelFactory chatModelFactory;
    private final AiConversationMessageService conversationMessageService;

    @Override
    public AiConversation createConversation(Long botId, String message) {
        AiBot aiBot = botService.getById(botId);
        if (aiBot == null) {
            throw new AiChatException("Bot不存在: " + botId);
        }
        if (!Boolean.TRUE.equals(aiBot.getStatus())) {
            throw new AiChatException("Bot已停用: " + aiBot.getName());
        }
        AiModel aiModel = modelService.getById(aiBot.getModelId());
        if (aiModel == null) {
            throw new AiChatException("模型不存在: " + aiBot.getModelId());
        }

        ChatModel chatModel = chatModelFactory.getChatModel(aiModel);
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
                SystemMessage.from(titleSystemPrompt),
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
