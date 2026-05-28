package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;


@Data
@Schema(description = "新增任务变量表单")
public class JobVariableCreateForm {

    @NotBlank
    @Length(max = 30)
    @Schema(description = "变量名")
    private String name;

    @NotBlank
    @Length(max = 100)
    @Schema(description = "变量值(支持SPEL表达式)")
    private String value;

    @NotNull
    @Schema(description = "是否启用")
    private Boolean status;
}
