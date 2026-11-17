package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新AI MCP工具表单（仅用户可配置字段，description/input_schema 由同步覆盖）")
public class AiMcpToolUpdateForm {

    @NotNull
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "自定义输出模板，传null表示清空")
    private String outputTemplate;
}
