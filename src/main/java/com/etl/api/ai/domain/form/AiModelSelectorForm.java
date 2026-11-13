package com.etl.api.ai.domain.form;

import com.etl.api.ai.domain.validator.AiModelSelectorFormValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@AiModelSelectorFormValidator
@Schema(description = "模型选择器表单")
public class AiModelSelectorForm {
    @Schema(description = "自增主键")
    private Long id;

    @NotBlank
    @Length(max = 256)
    @Schema(description = "大模型请求地址")
    private String baseUrl;

    @Length(max = 1000)
    @Schema(description = "大模型 API KEY")
    private String apiKey;
}
