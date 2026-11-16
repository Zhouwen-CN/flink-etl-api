package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AI对话表单")
public class AiChatForm {

    @NotNull
    @Schema(description = "Bot ID")
    private Long botId;

    @NotNull
    @Schema(description = "会话ID")
    private Long conversationId;

    @NotBlank
    @Schema(description = "对话内容")
    private String message;

    @Schema(description = "图片列表（base64，支持 data:image/png;base64,xxx 格式）")
    private List<String> images;
}
