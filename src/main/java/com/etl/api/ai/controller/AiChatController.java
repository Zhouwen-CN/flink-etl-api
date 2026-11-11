package com.etl.api.ai.controller;

import com.etl.api.ai.config.AiChatMemoryStore;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiChatForm;
import com.etl.api.ai.factory.ChatModelFactory;
import com.etl.api.ai.service.AiModelService;
import com.etl.api.ai.service.Assistant;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final AiChatMemoryStore chatMemoryStore;

    @Value("${custom.ai.max_memory_messages:100}")
    private Integer maxMemoryMessages;

    @PostMapping("/chat")
    public String model(@RequestBody @Validated AiChatForm form) {
        Long modelId = form.getModelId();
        AiModel aiModel = aiModelService.getById(modelId);
        if (aiModel == null) {
            throw new RuntimeException("model not found: " + modelId);
        }

        ChatModel chatModel = chatModelFactory.getChatModel(aiModel);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(id -> MessageWindowChatMemory.builder()
                        .id(id)
                        .maxMessages(maxMemoryMessages)
                        .chatMemoryStore(chatMemoryStore)
                        .build()
                )
                .build();
        return assistant.chat(form.getConversationId(), form.getMessage());
    }
}
