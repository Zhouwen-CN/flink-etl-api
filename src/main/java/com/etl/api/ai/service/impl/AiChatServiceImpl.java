package com.etl.api.ai.service.impl;

import com.etl.api.ai.config.AiAssistantService;
import com.etl.api.ai.config.AiChatMemoryStore;
import com.etl.api.ai.config.AiChatModelFactory;
import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.entity.AiBotKnowledge;
import com.etl.api.ai.domain.entity.AiBotMcpTool;
import com.etl.api.ai.domain.entity.AiMcpTool;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.dto.AiRetrievalContentDTO;
import com.etl.api.ai.domain.form.AiChatForm;
import com.etl.api.ai.exception.AiChatException;import com.etl.api.ai.service.AiBotService;
import com.etl.api.ai.service.AiChatService;
import com.etl.api.ai.service.AiConversationMessageService;
import com.etl.api.ai.service.AiModelService;
import com.etl.api.ai.service.manager.AiContentRetrieverManager;
import com.etl.api.ai.service.manager.AiMcpToolManager;
import com.etl.api.ai.util.AiMessageUtil;
import com.etl.api.ai.util.AiSseEmitterUtil;
import com.etl.api.sys.util.LocalDateTimeUtil;
import com.etl.api.sys.aop.IgnoreTransaction;
import com.mybatisflex.core.query.QueryChain;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.AiServiceTool;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private final AiContentRetrieverManager contentRetrieverManager;
    private final AiConversationMessageService conversationMessageService;
    @Value("${custom.ai.chat.max-memory-messages:100}")
    private Integer maxMemoryMessages;

    @Override
    @IgnoreTransaction
    public SseEmitter streamChat(AiChatForm form) {
        // bot
        AiBot aiBot = botService.getById(form.getBotId());
        if (aiBot == null) {
            throw new AiChatException("Bot不存在: " + form.getBotId());
        }
        if (!Boolean.TRUE.equals(aiBot.getStatus())) {
            throw new AiChatException("Bot已停用: " + aiBot.getName());
        }
        // model
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
        Long conversationId = form.getConversationId();
        sse.onError(ex -> cancelQuietly(handleRef, conversationId));
        sse.onTimeout(() -> {
            log.warn("SSE timeout, conversationId={}", conversationId);
            cancelQuietly(handleRef, conversationId);
        });

        // 获取 ai service
        AiServices<AiAssistantService> aiServices = AiServices.builder(AiAssistantService.class)
                .streamingChatModel(chatModelFactory.getStreamingChatModel(aiModel))
                // 系统提示词
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
                // 记忆
                .chatMemoryProvider(id -> MessageWindowChatMemory.builder()
                        .id(id)
                        .maxMessages(maxMemoryMessages)
                        // 系统提示词含当前时间，每轮内容都变化；不固定首位会被替换到列表末尾
                        .alwaysKeepSystemMessageFirst(true)
                        .chatMemoryStore(chatMemoryStore)
                        .build()
                )
                // 工具
                .tools(this.getToolList(aiBot.getId()))
                // 工具调用往返轮数
                .maxToolCallingRoundTrips(aiBot.getMaxToolCalls())
                // 把工具 参数错误信息 和 调用错误信息 发送给 LLM
                .toolArgumentsErrorHandler(ToolArgumentsErrorHandler.sendExceptionMessageToLlm())
                .toolExecutionErrorHandler(ToolExecutionErrorHandler.failInvocationUnlessVisibleToLlm())
                // 控制存储在聊天记忆中的是 增强的 用户消息（注入了检索到的 Content）还是 原始的 用户消息
                .storeRetrievedContentInChatMemory(false);

        // 内容检索（注入阶段将资料转为 DTO 写入 retrievalContents，提示词与落库同一份数据）
        List<AiRetrievalContentDTO> retrievalContents = new CopyOnWriteArrayList<>();
        RetrievalAugmentor retrievalAugmentor = this.getRetrievalAugmentor(aiBot.getId(), retrievalContents);
        if (retrievalAugmentor != null) {
            aiServices.retrievalAugmentor(retrievalAugmentor);
        }
        AiAssistantService assistantService = aiServices.build();

        // 构建用户消息内容列表（文本 + 图片）
        List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(form.getMessage()));
        contents.addAll(AiMessageUtil.buildImageContents(form.getImages()));

        // stream chat
        TokenStream tokenStream = assistantService.streamingChat(
                conversationId,
                contents,
                this.getRequestParams(aiBot)
        );

        // 回调
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
                // 完成时：assistant 行已落库（LC4j addToMemory 先于本回调，已核实源码），
                // 回写 attributes（tokenUsage + retrievalContent）后发送 done 事件
                .onCompleteResponse(response -> {
                    Map<String, Object> attributes = AiMessageUtil.toAttributes(response, retrievalContents, aiModel.getContextWindow());
                    try {
                        conversationMessageService.writeAssistantAttributes(conversationId, attributes);
                    } catch (Exception e) {
                        log.warn("assistant attributes 回写失败, conversationId={}", conversationId, e);
                    }
                    sse.sendDone(AiMessageUtil.withAttributes(attributes));
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

    /**
     * 获取 内容检索
     */
    private RetrievalAugmentor getRetrievalAugmentor(Long botId, List<AiRetrievalContentDTO> retrievalContents) {
        List<Long> knowledgeIds = QueryChain.of(AiBotKnowledge.class)
                .select(AiBotKnowledge::getKnowledgeId)
                .eq(AiBotKnowledge::getBotId, botId)
                .listAs(Long.class);

        return contentRetrieverManager.getRetrievalAugmentor(knowledgeIds, retrievalContents);
    }

    /**
     * 获取 请求参数
     */
    private ChatRequestParameters getRequestParams(AiBot aiBot) {
        return ChatRequestParameters.builder()
                .temperature(aiBot.getTemperature())
                .topP(aiBot.getTopP())
                .build();
    }

    /**
     * 获取 工具
     */
    private List<AiServiceTool> getToolList(Long botId) {
        val toolIds = QueryChain.of(AiBotMcpTool.class)
                .select(AiBotMcpTool::getToolId)
                .eq(AiBotMcpTool::getBotId, botId)
                .listAs(Long.class);

        if (CollectionUtils.isEmpty(toolIds)) {
            return List.of();
        }

        List<AiMcpTool> mcpToolList = QueryChain.of(AiMcpTool.class)
                .in(AiMcpTool::getId, toolIds)
                .list();

        return mcpToolManager.build(mcpToolList);
    }
}
