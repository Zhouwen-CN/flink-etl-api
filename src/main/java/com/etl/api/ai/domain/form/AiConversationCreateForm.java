package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建AI会话表单")
public class AiConversationCreateForm {

    @NotNull
    @Schema(description = "模型ID")
    private Long modelId;

    @NotBlank
    @Schema(description = "用户第一条消息")
    private String message;
}
