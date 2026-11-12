package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "AI会话重命名表单")
public class AiConversationRenameForm {

    @NotNull
    @Schema(description = "会话ID")
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "会话标题")
    private String title;
}
