package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "AI 向量数据库连接验证表单（支持未保存配置的验证）")
public class AiVectorDatabaseVerifyForm {

    @NotNull
    @Schema(description = "向量数据库类型（1-redis 2-milvus）")
    private Integer type;

    @NotBlank
    @Schema(description = "连接配置JSON")
    private String configJson;
}
