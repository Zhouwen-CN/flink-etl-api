package com.etl.api.ai.service.impl;

import com.etl.api.ai.config.AiAssistantService;
import com.etl.api.ai.config.AiChatMemoryStore;
import com.etl.api.ai.config.AiChatModelFactory;
import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiChatForm;
import com.etl.api.ai.exception.AiChatException;
import com.etl.api.ai.service.AiBotService;
import com.etl.api.ai.service.AiChatService;
import com.etl.api.ai.service.AiModelService;
import com.etl.api.ai.service.manager.AiMcpToolManager;
import com.etl.api.ai.util.AiMessageUtil;
import com.etl.api.ai.util.AiSseEmitterUtil;
import com.etl.api.sys.util.LocalDateTimeUtil;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.AiServiceTool;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI聊天 服务层实现。
 *
 * <p>流式聊天：Bot/模型校验 → AiServices 构建（系统提示词 + 环境信息、会话记忆、MCP 工具、
 * 工具错误回传 LLM）→ TokenStream 事件推送 SSE，断开/超时静默取消生成。
 *
 * @author chen
 * @since 2026-11-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final AiChatModelFactory chatModelFactory;
    private final AiChatMemoryStore chatMemoryStore;
    private final AiBotService botService;
    private final AiModelService modelService;
    private final AiMcpToolManager mcpToolManager;
    @Value("${custom.ai.chat.max-memory-messages:100}")
    private Integer maxMemoryMessages;

    @Override
    public SseEmitter streamChat(AiChatForm form) {
        Long conversationId = form.getConversationId();

        AiBot aiBot = botService.getById(form.getBotId());
        if (aiBot == null) {
            throw new AiChatException("Bot不存在: " + form.getBotId());
        }
        if (!Boolean.TRUE.equals(aiBot.getStatus())) {
            throw new AiChatException("Bot已停用: " + aiBot.getName());
        }
        AiModel aiModel = modelService.getById(aiBot.getModelId());
        if (aiModel == null) {
            throw new AiChatException("模型不存在: " + aiBot.getModelId());
        }
        if (form.getImages() != null && !form.getImages().isEmpty() && !Boolean.TRUE.equals(aiModel.getSupportImage())) {
            throw new AiChatException("模型不支持图片: " + aiBot.getName());
        }

        AiSseEmitterUtil sse = new AiSseEmitterUtil();
        AtomicReference<StreamingHandle> handleRef = new AtomicReference<>();

        // 客户端断开/超时 → 取消模型生成，避免空跑浪费 token
        // 前端 abort 后服务端在下一次写入时感知断开，此处负责触发取消
        sse.onError(ex -> cancelQuietly(handleRef, conversationId));
        sse.onTimeout(() -> {
            log.warn("SSE timeout, conversationId={}", conversationId);
            cancelQuietly(handleRef, conversationId);
        });

        // 聊天工具集合：MCP 工具（ToolSpecification 来自本地缓存 input_schema，执行时临时建连调用远端）
        // + 知识库检索工具
        List<AiServiceTool> chatTools = mcpToolManager.build(aiBot.getId());

        // 获取 ai service
        AiAssistantService assistantService = AiServices.builder(AiAssistantService.class)
                .streamingChatModel(chatModelFactory.getStreamingChatModel(aiModel))
                // 环境信息固定注入 provider（用户未配置系统提示词时也不丢环境信息）；
                .systemMessageProvider(id -> {
                    String now = LocalDateTimeUtil.format(LocalDateTime.now());
                    String osInfo = System.getProperty("os.name") + " " + System.getProperty("os.version");
                    return """
                            # 环境信息
                            - 当前时间: %s (%s)
                            - 操作系统: %s
                            """.formatted(now, ZoneId.systemDefault(), osInfo);
                })
                .systemMessageTransformer(systemMessage ->
                        StringUtils.hasText(aiBot.getSystemPrompt())
                                ? systemMessage + "\n\n" + aiBot.getSystemPrompt()
                                : systemMessage
                )
                .chatMemoryProvider(id -> MessageWindowChatMemory.builder()
                        .id(id)
                        .maxMessages(maxMemoryMessages)
                        .chatMemoryStore(chatMemoryStore)
                        .build()
                )
                .tools(chatTools)
                // 工具调用往返轮数
                .maxToolCallingRoundTrips(aiBot.getMaxToolCalls())
                // 把工具 参数错误信息 和 调用错误信息 发送给 LLM
                .toolArgumentsErrorHandler(ToolArgumentsErrorHandler.sendExceptionMessageToLlm())
                .toolExecutionErrorHandler(ToolExecutionErrorHandler.failInvocationUnlessVisibleToLlm())
                .build();

        // 获取 chat 请求参数
        ChatRequestParameters chatRequestParameters = ChatRequestParameters.builder()
                .temperature(aiBot.getTemperature())
                .topP(aiBot.getTopP())
                .build();

        // 构建用户消息内容列表（文本 + 图片）
        List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(form.getMessage()));
        contents.addAll(AiMessageUtil.buildImageContents(form.getImages()));

        // stream chat
        TokenStream tokenStream = assistantService.streamingChat(
                conversationId,
                contents,
                chatRequestParameters
        );

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
                // 工具执行前：推送调用请求（工具名 + 参数）
                .beforeToolExecution(before -> sse.sendTool(AiMessageUtil.toolCall(before.request())))
                // 工具执行后：推送执行结果
                .onToolExecuted(execution -> sse.sendTool(AiMessageUtil.toolResult(execution)))
                .onCompleteResponse(response -> sse.sendDone(AiMessageUtil.metadata(response, aiModel.getContextWindow())))
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
}
