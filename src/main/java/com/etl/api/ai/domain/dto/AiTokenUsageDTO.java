package com.etl.api.ai.domain.dto;

import dev.langchain4j.model.output.TokenUsage;
import lombok.Data;
import lombok.val;

@Data
public class AiTokenUsageDTO {
    private Integer inputToken;
    private Integer outputToken;
    private Integer totalToken;
    private Integer contextWindow;


    public static AiTokenUsageDTO from(TokenUsage tokenUsage, Integer contextWindow) {
        val aiTokenUsageDTO = new AiTokenUsageDTO();
        aiTokenUsageDTO.setInputToken(tokenUsage.inputTokenCount());
        aiTokenUsageDTO.setOutputToken(tokenUsage.outputTokenCount());
        aiTokenUsageDTO.setTotalToken(tokenUsage.totalTokenCount());
        aiTokenUsageDTO.setContextWindow(contextWindow);
        return aiTokenUsageDTO;
    }
}
