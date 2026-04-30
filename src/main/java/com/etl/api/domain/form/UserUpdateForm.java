package com.etl.api.domain.form;


import com.etl.api.enumeration.GenderEnum;
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

    @NotBlank
    @Length(max = 30)
    @Schema(description = "用户昵称", maxLength = 30)
    private String nickname;

    @NotNull
    @Schema(description = "用户性别(0未知 1男 2女)")
    private GenderEnum gender;

    @NotNull
    @Schema(description = "账号状态(1启用 0停用)")
    private Boolean status;

}
