package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增AI MCP服务表单")
public class AiMcpServerCreateForm {

    @NotBlank
    @Length(max = 50)
    @Schema(description = "mcp服务名称")
    private String name;

    @Length(max = 255)
    @Schema(description = "mcp服务描述")
    private String description;

    @NotBlank
    @Schema(description = "mcp连接配置JSON（单个服务，仅允许远程传输）")
    private String configJson;
}
