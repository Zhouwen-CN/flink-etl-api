package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "更新权限表单")
public class PermissionUpdateForm extends PermissionCreateForm {

    @NotNull
    @Schema(description = "自增主键")
    private Long id;
}
