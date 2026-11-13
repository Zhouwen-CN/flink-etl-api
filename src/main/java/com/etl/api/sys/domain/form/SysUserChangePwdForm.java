package com.etl.api.sys.domain.form;


import com.etl.api.sys.domain.validator.SysUserChangePwdFormValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@ToString
@SysUserChangePwdFormValidator
@Schema(description = "修改密码表单")
public class SysUserChangePwdForm {

    @NotNull
    @Length(max = 30)
    @Schema(description = "旧密码")
    private String oldPwd;

    @NotNull
    @Length(max = 30)
    @Schema(description = "新密码")
    private String newPwd;

    @NotNull
    @Length(max = 30)
    @Schema(description = "确认密码")
    private String confirmPwd;
}
