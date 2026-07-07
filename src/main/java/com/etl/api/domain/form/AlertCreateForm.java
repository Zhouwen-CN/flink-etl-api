package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@Schema(description = "新增任务状态告警表单")
public class AlertCreateForm {

    @NotNull
    @Length(max = 50)
    @Schema(description = "告警名称")
    private String name;

    @NotNull
    @Email
    @Schema(description = "邮件地址")
    private String email;

    @NotNull
    @Size(min = 1)
    @Schema(description = "任务id列表")
    private List<Long> jobIds;
}
