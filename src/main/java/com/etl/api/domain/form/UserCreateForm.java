package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@Schema(description = "新增用户表单")
public class UserCreateForm {
    @NotBlank
    @Length(max = 30)
    @Schema(description = "用户名", maxLength = 30)
    private String username;

    @NotBlank
    @Length(max = 30)
    @Schema(description = "密码", maxLength = 60)
    private String password;

    @NotBlank
    @Length(max = 30)
    @Schema(description = "用户昵称", maxLength = 30)
    private String nickname;

    @NotNull
    @Schema(description = "用户性别(0未知 1男 2女)")
    private Integer gender;

    @NotNull
    @Schema(description = "账号状态(1启用 0停用)")
    private Boolean status;

    @NotNull
    @Schema(description = "用户角色ID列表")
    private List<Long> roleIds;

}
