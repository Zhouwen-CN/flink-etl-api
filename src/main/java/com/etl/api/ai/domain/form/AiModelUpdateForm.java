package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新的时候，apikey 可以为空
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新AI模型表单")
public class AiModelUpdateForm extends AiModelCreateForm {

    @NotNull
    @Schema(description = "自增主键")
    private Long id;
}
