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
import com.etl.api.ai.service.AssistantService;
import com.etl.api.ai.util.AiMessageUtil;
import com.etl.api.ai.util.SseEmitterUtil;
import com.etl.api.domain.vo.ResponseVO;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
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
    @Operation(summary = "聊天（SSE 流式）")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody @Validated AiChatForm form) {
        Long conversationId = form.getConversationId();

        AiModel aiModel = modelService.getById(form.getModelId());
        if (aiModel == null) {
            throw new RuntimeException("model not found: " + form.getModelId());
        }

        SseEmitterUtil sse = new SseEmitterUtil();
        AtomicReference<StreamingHandle> handleRef = new AtomicReference<>();

        // 客户端断开/超时 → 取消模型生成，避免空跑浪费 token
        // 前端 abort 后服务端在下一次写入时感知断开，此处负责触发取消
        sse.onError(ex -> cancelQuietly(handleRef, conversationId));
        sse.onTimeout(() -> {
            log.warn("SSE timeout, conversationId={}", conversationId);
            cancelQuietly(handleRef, conversationId);
        });

        AssistantService assistantService = buildAssistant(chatModelFactory.getStreamingChatModel(aiModel));

        // todo: 请求参数，按需修改
        ChatRequestParameters parameters = ChatRequestParameters.builder().build();
        TokenStream tokenStream = assistantService.streamingChat(conversationId, form.getMessage(), parameters);

        tokenStream
                .onPartialThinkingWithContext((partial, ctx) -> {
                    handleRef.set(ctx.streamingHandle());
                    sse.sendDelta(AiMessageUtil.thinking(partial.text()));
                })
                // 官方推荐：onPartialResponseWithContext 可拿到 StreamingHandle 用于取消
                .onPartialResponseWithContext((partial, ctx) -> {
                    handleRef.set(ctx.streamingHandle());
                    sse.sendDelta(AiMessageUtil.text(partial.text()));
                })
                .onCompleteResponse(response -> {
                    sse.sendDone(AiMessageUtil.metadata(response));
                })
                .onError(ex -> sse.sendError(ex.getMessage()))
                .start();

        return sse.getEmitter();
    }

    /**
     * 连接断开/超时时的静默取消，只负责停生成，不再向已断开的连接写数据
     */
    private void cancelQuietly(AtomicReference<StreamingHandle> handleRef, Long conversationId) {
        StreamingHandle handle = handleRef.get();
        if (handle != null) {
            try {
                handle.cancel();
            } catch (Exception e) {
                log.warn("cancel streaming failed, conversationId={}", conversationId, e);
            }
        }
    }

    private AssistantService buildAssistant(StreamingChatModel streamingChatModel) {
        return AiServices.builder(AssistantService.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(id -> MessageWindowChatMemory.builder()
                        .id(id)
                        .maxMessages(maxMemoryMessages)
                        .chatMemoryStore(chatMemoryStore)
                        .build()
                )
                .build();
    }
}
