package com.etl.api.ai.controller;

import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiChatForm;
import com.etl.api.ai.factory.ChatModelFactory;
import com.etl.api.ai.service.AiModelService;
import com.etl.api.ai.service.Assistant;
import com.etl.api.util.SaSessionUtil;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiChatController {

    private final ChatModelFactory chatModelFactory;
    private final AiModelService aiModelService;
    private final MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.withMaxMessages(100);

    @PostMapping("/chat")
    public String model(@RequestBody @Validated AiChatForm form) {
        Long modelId = form.getModelId();
        AiModel aiModel = aiModelService.getById(modelId);
        if (aiModel == null) {
            throw new RuntimeException("model not found: " + modelId);
        }

        String username = SaSessionUtil.getUserInfo().getUsername();
        String conversationId = form.getConversationId();
        String memoryId = username + ":" + conversationId;

        ChatModel chatModel = chatModelFactory.getChatModel(aiModel);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(id -> messageWindowChatMemory)
                .build();

        return assistant.chat(memoryId, form.getMessage());
    }
}
