package com.etl.api.domain.form;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "用户修改表单")
public class UserUpdateForm {

    @NotNull
    @Schema(description = "用户ID")
    private Long id;

    @NotBlank
    @Length(max = 30)
    @Schema(description = "用户名", maxLength = 30)
    private String username;

    @NotNull
    @Schema(description = "用户性别(0未知 1男 2女)")
    private Integer gender;

    @Length(max = 65535)
    @Schema(description = "用户头像(base64)", maxLength = 65535)
    private String avatar;
}
