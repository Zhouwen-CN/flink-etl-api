package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新任务状态告警表单")
public class AlertUpdateForm extends AlertCreateForm {

    @NotNull
    @Schema(description = "自增主键")
    private Long id;
}
