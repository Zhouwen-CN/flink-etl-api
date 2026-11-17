package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新AI MCP服务表单")
public class AiMcpServerUpdateForm extends AiMcpServerCreateForm {

    @NotNull
    @Schema(description = "ID")
    private Long id;
}
