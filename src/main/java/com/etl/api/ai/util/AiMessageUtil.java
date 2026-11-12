package com.etl.api.ai.util;


import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;

import java.util.Collections;
import java.util.Map;

public final class AiMessageUtil {

    private AiMessageUtil() {
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

    public static String toolRequest(ToolExecutionRequest toolExecutionRequest) {
        AiMessage aiMessage = AiMessage.builder()
                .toolExecutionRequests(Collections.singletonList(toolExecutionRequest))
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
