package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "用户登入表单")
public class UserLoginForm {
    @NotBlank
    @Length(max = 30)
    @Schema(description = "用户名", maxLength = 30)
    private String username;

    @NotBlank
    @Length(max = 60)
    @Schema(description = "密码", maxLength = 60)
    private String password;

    @NotBlank
    @Schema(description = "验证码ID")
    private String captchaId;

    @NotBlank
    @Length(min = 4, max = 4)
    @Schema(description = "验证码", minLength = 4, maxLength = 4)
    private String code;
}
