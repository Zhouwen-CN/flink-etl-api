package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增Flink集群表单")
public class FlinkClusterCreateForm {

    @NotBlank
    @Length(max = 30)
    @Schema(description = "集群名称")
    private String name;

    @NotBlank
    @Length(max = 100)
    @Pattern(regexp = "^https?://[a-zA-Z0-9.-]+(:\\d+)?$")
    @Schema(description = "集群地址")
    private String jobManagerUrl;

    @NotNull
    @Schema(description = "集群状态")
    private Boolean status;
}
