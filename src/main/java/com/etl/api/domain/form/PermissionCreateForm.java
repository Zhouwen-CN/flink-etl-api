package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增权限表单")
public class PermissionCreateForm {

    @NotBlank
    @Length(max = 30)
    @Schema(description = "权限名称")
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-z]+\\.[a-z]+$", message = "权限标识符格式错误")
    @Schema(description = "权限标识符")
    private String code;
}
