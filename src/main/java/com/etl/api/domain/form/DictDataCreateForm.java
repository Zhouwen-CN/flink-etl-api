package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增字典数据表单")
public class DictDataCreateForm {

    @NotNull
    @Schema(description = "字典类型id")
    private Long typeId;

    @NotBlank
    @Length(max = 30)
    @Schema(description = "字典键")
    private String label;

    @NotNull
    @Schema(description = "字典值")
    private Long value;

    @NotNull
    @Schema(description = "字典排序")
    private Integer sortId;
}
