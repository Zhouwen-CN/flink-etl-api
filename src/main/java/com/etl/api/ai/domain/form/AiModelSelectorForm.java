package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "模型选择器表单")
public class AiModelSelectorForm {

    @NotNull
    @Length(max = 256)
    @Pattern(regexp = "^https?://.*[^/]$", message = "baseUrl 必须以 http:// 或 https:// 开头，且不能以 / 结尾")
    @Schema(description = "大模型请求地址")
    private String baseUrl;

    @NotBlank
    @Length(max = 1000)
    @Schema(description = "大模型 API KEY")
    private String apiKey;
}
