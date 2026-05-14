package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "ETL任务提交表单")
public class EtlJobSubmitForm {

    @NotNull
    @Schema(description = "任务id")
    private Long id;

    @Schema(description = "检查点/保存点路径")
    private String savepointPath;
}
