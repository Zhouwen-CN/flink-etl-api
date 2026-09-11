package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "实例重新映射 表单")
public class JobInstanceRemapForm {
    @NotNull
    @Size(min = 1)
    @Schema(description = "ID 列表")
    private List<String> ids;
}
