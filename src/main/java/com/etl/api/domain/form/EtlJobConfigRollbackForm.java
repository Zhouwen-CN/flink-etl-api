package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "ETL任务配置回滚表单")
public class EtlJobConfigRollbackForm {

    @NotNull
    @Schema(description = "任务id")
    private Long jobId;

    @NotNull
    @Schema(description = "回滚目标版本号")
    private Integer version;
}
