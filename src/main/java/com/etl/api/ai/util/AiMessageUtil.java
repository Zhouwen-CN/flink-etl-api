package com.etl.api.ai.util;


import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.tool.ToolExecution;

import java.util.ArrayList;
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

    public static String metadata(ChatResponse response, Integer contextWindow) {
        TokenUsage tokenUsage = response.tokenUsage();
        Integer inputTokenCount = tokenUsage.inputTokenCount();
        Integer outputTokenCount = tokenUsage.outputTokenCount();
        Integer totalTokenCount = tokenUsage.totalTokenCount();

        AiMessage aiMessage = AiMessage.builder()
                .attributes(
                        Map.of(
                                "inputToken", inputTokenCount,
                                "outputToken", outputTokenCount,
                                "totalToken", totalTokenCount,
                                "contextWindow", contextWindow
                        )
                ).build();
        return ChatMessageSerializer.messageToJson(aiMessage);
    }
}
