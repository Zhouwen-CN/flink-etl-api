package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新AI 向量数据库表单")
public class AiVectorDatabaseUpdateForm extends AiVectorDatabaseCreateForm {

    @NotNull
    @Schema(description = "ID")
    private Long id;
}
