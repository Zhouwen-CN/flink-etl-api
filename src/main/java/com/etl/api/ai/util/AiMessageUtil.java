package com.etl.api.ai.util;


import com.etl.api.ai.domain.dto.AiRetrievalContentDTO;
import com.etl.api.ai.domain.dto.AiTokenUsageDTO;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.tool.ToolExecution;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AiMessageUtil {

    /**
     * 未携带 data: 前缀时图片 mimeType 的兜底默认值
     */
    private static final String DEFAULT_IMAGE_MIME_TYPE = "image/png";

    private AiMessageUtil() {
    }

    /**
     * 将 base64 图片列表转为 ImageContent，支持 data:image/png;base64,xxx 格式
     */
    public static List<ImageContent> buildImageContents(List<String> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        List<ImageContent> contents = new ArrayList<>();
        for (String image : images) {
            String mimeType = DEFAULT_IMAGE_MIME_TYPE;
            String base64Data = image;
            // 兼容 data:image/png;base64,xxx 格式
            if (image.startsWith("data:")) {
                int base64Start = image.indexOf(";base64,");
                if (base64Start > 0) {
                    mimeType = image.substring(5, base64Start);
                    base64Data = image.substring(base64Start + ";base64,".length());
                }
            }
            contents.add(ImageContent.from(base64Data, mimeType));
        }
        return contents;
    }


    public static String text(String text) {
        AiMessage aiMessage = AiMessage.builder()
                .text(text)
                .build();

        return ChatMessageSerializer.messageToJson(aiMessage);
    }

    public static String thinking(String thinking) {
        AiMessage aiMessage = AiMessage.builder()
                .thinking(thinking)
                .build();

        return ChatMessageSerializer.messageToJson(aiMessage);
    }

    /**
     * 工具调用请求信息（执行前推送），前端可展示"正在调用工具"及参数
     */
    public static String toolCall(ToolExecutionRequest request) {
        AiMessage aiMessage = AiMessage.builder()
                .toolExecutionRequests(List.of(request))
                .build();

        return ChatMessageSerializer.messageToJson(aiMessage);
    }

    /**
     * 工具调用结果信息（执行后推送），放在 attributes 中与普通文本区分
     */
    public static String toolResult(ToolExecution execution) {
        ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.toolExecutionResultMessage(
                execution.request(),
                execution.result()
        );
        return ChatMessageSerializer.messageToJson(toolExecutionResultMessage);
    }

    /**
     * 构造带 attributes 的 assistant 消息 JSON（done 事件载荷与落库回写复用同一份）：
     * 以 response.aiMessage() 的 text/thinking/toolExecutionRequests 原样重建，
     * attributes 携带 tokenUsage 与 retrievalContent（attributes 不发给大模型，仅前端展示用）。
     *
     * <p>值仅限 JSON 基本类型与嵌套对象/数组，保证序列化往返稳定
     * （记忆 updateMessages 的前缀比对依赖 round-trip 一致）。
     */
    public static String withAttributes(ChatResponse response,
                                        List<AiRetrievalContentDTO> retrievalContentList,
                                        Integer contextWindow) {
        AiMessage aiMessage = response.aiMessage();

        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("tokenUsage", AiTokenUsageDTO.from(response.tokenUsage(), contextWindow));
        attributes.put("retrievalContent", retrievalContentList);

        var builder = AiMessage.builder().attributes(attributes);
        if (aiMessage.text() != null) {
            builder.text(aiMessage.text());
        }
        if (aiMessage.thinking() != null) {
            builder.thinking(aiMessage.thinking());
        }
        if (aiMessage.toolExecutionRequests() != null) {
            builder.toolExecutionRequests(aiMessage.toolExecutionRequests());
        }
        return ChatMessageSerializer.messageToJson(builder.build());
    }
}
