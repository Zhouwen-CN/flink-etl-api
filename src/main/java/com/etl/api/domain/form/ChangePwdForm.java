package com.etl.api.domain.form;


import com.etl.api.domain.validator.FieldMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@ToString
@FieldMatch(
        field = "newPwd",
        confirmField = "confirmPwd",
        message = "两次输入的密码不一致"
)
@Schema(description = "修改密码表单")
public class ChangePwdForm {

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
