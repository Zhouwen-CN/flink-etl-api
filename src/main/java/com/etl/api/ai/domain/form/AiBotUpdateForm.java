package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新AI Bot表单")
public class AiBotUpdateForm extends AiBotCreateForm {

    @NotNull
    @Schema(description = "自增主键")
    private Long id;
}
