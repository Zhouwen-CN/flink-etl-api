package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "AI对话表单")
public class AiChatForm {

    @NotNull
    @Schema(description = "模型ID")
    private Long modelId;

    @NotNull
    @Schema(description = "会话ID")
    private Long conversationId;

    @NotBlank
    @Schema(description = "对话内容")
    private String message;
}
