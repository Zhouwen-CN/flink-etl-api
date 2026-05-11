package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增Flink集群表单")
public class FlinkClusterCreateForm {

    @NotBlank
    @Length(max = 30)
    @Schema(description = "集群名称")
    private String name;

    @NotNull
    @Length(max = 20)
    @Schema(description = "集群ip地址")
    private String ip;

    @NotNull
    @Schema(description = "集群端口")
    private Integer port;

    @NotNull
    @Schema(description = "集群状态")
    private Boolean status;
}
