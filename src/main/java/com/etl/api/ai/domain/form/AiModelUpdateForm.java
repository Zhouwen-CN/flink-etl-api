package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "更新AI模型表单")
public class AiModelUpdateForm {

    @NotNull
    @Schema(description = "自增主键")
    private Long id;

    @NotBlank
    @Length(max = 64)
    @Schema(description = "模型唯一标识")
    private String name;

    @NotNull
    @Schema(description = "接口格式: 1-OpenAI Chat Completions, 2-OpenAI Responses, 3-Anthropic Messages")
    private Integer format;

    @Length(max = 256)
    @Schema(description = "大模型请求地址")
    private String baseUrl;

    @Length(max = 1000)
    @Schema(description = "大模型 API KEY")
    private String apiKey;

    @Length(max = 64)
    @Schema(description = "模型名称，如 gpt-4o-mini")
    private String modelName;
}
