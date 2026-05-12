package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增字典类型表单")
public class DictTypeCreateForm {

    @NotBlank
    @Length(max = 30)
    @Schema(description = "字典名称")
    private String name;

    @NotBlank
    @Length(max = 30)
    @Schema(description = "字典描述")
    private String remark;

    @NotNull
    @Schema(description = "字典是否启用")
    private Boolean status;
}
