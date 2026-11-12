package com.etl.api.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.etl.api.ai.config.AiChatMemoryStore;
import com.etl.api.ai.domain.entity.AiConversation;
import com.etl.api.ai.domain.entity.AiConversationMessage;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiChatForm;
import com.etl.api.ai.domain.form.AiConversationCreateForm;
import com.etl.api.ai.domain.vo.AiConversationVO;
import com.etl.api.ai.factory.ChatModelFactory;
import com.etl.api.ai.service.AiConversationMessageService;
import com.etl.api.ai.service.AiConversationService;
import com.etl.api.ai.service.AiModelService;
import com.etl.api.ai.service.Assistant;
import com.etl.api.domain.vo.ResponseVO;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AI聊天 控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/chat")
public class AiChatController {

    private final ChatModelFactory chatModelFactory;
    private final AiModelService modelService;
    private final AiChatMemoryStore chatMemoryStore;
    private final AiConversationService conversationService;
    private final AiConversationMessageService conversationMessageService;

    @Value("${custom.ai.max_memory_messages:100}")
    private Integer maxMemoryMessages;

    @SaCheckPermission("chat.select")
    @Operation(summary = "查询会话列表")
    @GetMapping("/conversation")
    public ResponseVO<List<AiConversationVO>> listConversation() {
        List<AiConversationVO> vos = conversationService.queryChain()
                .eq(AiConversation::getUserId, StpUtil.getLoginIdAsLong())
                .orderBy(AiConversation::getCreateTime, false)
                .listAs(AiConversationVO.class);
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("chat.select")
    @Operation(summary = "查询会话消息列表")
    @GetMapping("/conversation/message")
    public ResponseVO<List<String>> listConversationMessage(
            @RequestParam("conversationId") Long conversationId
    ) {
        List<String> messages = conversationMessageService.queryChain()
                .select(AiConversationMessage::getMessage)
                .eq(AiConversationMessage::getConversationId, conversationId)
                .orderBy(AiConversationMessage::getId, true)
                .listAs(String.class);
        return ResponseVO.ok(messages);
    }

    @SaCheckPermission("chat.insert")
    @Operation(summary = "新增会话")
    @PostMapping("/conversation")
    public ResponseVO<AiConversationVO> createConversation(@RequestBody @Validated AiConversationCreateForm form) {
        AiConversation conversation = conversationService.createConversation(form.getModelId(), form.getMessage());
        AiConversationVO vo = new AiConversationVO();
        vo.setId(conversation.getId());
        vo.setTitle(conversation.getTitle());
        return ResponseVO.ok(vo);
    }

    @SaCheckPermission("chat.insert")
    @Operation(summary = "聊天")
    @PostMapping
    public String model(@RequestBody @Validated AiChatForm form) {
        Long modelId = form.getModelId();
        AiModel aiModel = modelService.getById(modelId);
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
