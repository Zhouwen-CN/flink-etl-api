package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增AI模型表单")
public class AiModelCreateForm {

    @NotBlank
    @Length(max = 64)
    @Schema(description = "模型唯一标识")
    private String name;

    @Schema(description = "接口格式: 1-OpenAI Chat Completions, 2-OpenAI Responses, 3-Anthropic Messages")
    private Integer format;

    @NotNull
    @Length(max = 256)
    @Pattern(regexp = "^https?://.*[^/]$", message = "baseUrl 必须以 http:// 或 https:// 开头，且不能以 / 结尾")
    @Schema(description = "大模型请求地址")
    private String baseUrl;

    @NotBlank
    @Length(max = 1000)
    @Schema(description = "大模型 API KEY")
    private String apiKey;

    @NotBlank
    @Length(max = 64)
    @Schema(description = "模型名称，如 gpt-4o-mini")
    private String modelName;

    @NotNull
    @PositiveOrZero
    @Schema(description = "上下文窗口")
    private Integer contextWindow;

    @NotNull
    @Schema(description = "是否支持图片")
    private Boolean supportImage;
}
