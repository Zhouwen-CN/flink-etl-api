package com.etl.api.domain.form;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户修改表单")
public class UserUpdateForm extends UserCreateForm {

    @NotNull
    @Schema(description = "用户ID")
    private Long id;
}
