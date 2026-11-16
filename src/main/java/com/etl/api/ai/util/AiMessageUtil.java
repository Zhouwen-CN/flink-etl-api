package com.etl.api.ai.util;


import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;

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

    public static String metadata(ChatResponse response) {
        String text = response.aiMessage().text();
        TokenUsage tokenUsage = response.tokenUsage();
        Integer inputTokenCount = tokenUsage.inputTokenCount();
        Integer outputTokenCount = tokenUsage.outputTokenCount();
        Integer totalTokenCount = tokenUsage.totalTokenCount();

        AiMessage aiMessage = AiMessage.builder()
                .attributes(
                        Map.of(
                                "rawMessage", text,
                                "inputToken", inputTokenCount,
                                "outputToken", outputTokenCount,
                                "totalToken", totalTokenCount
                        )
                ).build();
        return ChatMessageSerializer.messageToJson(aiMessage);
    }
}
