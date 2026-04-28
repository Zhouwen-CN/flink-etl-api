package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增用户表单")
public class UserCreateForm {
    @NotBlank
    @Length(max = 30)
    @Schema(description = "用户名", maxLength = 30)
    private String username;

    @NotBlank
    @Length(max = 60)
    @Schema(description = "密码", maxLength = 60)
    private String password;

    @NotNull
    @Schema(description = "用户性别(0未知 1男 2女)")
    private Integer gender;

    @Length(max = 16383)
    @Schema(description = "用户头像(base64)", maxLength = 16383)
    private String avatar;

}
