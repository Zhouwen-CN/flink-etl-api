package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增ETL任务表单")
public class EtlJobCreateForm {

    @NotBlank
    @Length(max = 30)
    @Schema(description = "任务名称")
    private String name;

    @NotNull
    @Schema(description = "flink集群id")
    private Long clusterId;

    @NotNull
    @Schema(description = "jar包id")
    private Long jarId;

    @NotBlank
    @Length(max = 65535)
    @Schema(description = "json配置")
    private String config;

    @NotNull
    @Schema(description = "任务类型")
    private Integer type;
}
