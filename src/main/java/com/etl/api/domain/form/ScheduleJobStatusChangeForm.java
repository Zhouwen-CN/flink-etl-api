package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "定时任务状态变更表单")
public class ScheduleJobStatusChangeForm {

    @NotNull
    @Schema(description = "自增主键")
    private Long id;

    @NotNull
    @Schema(description = "是否开启")
    private Boolean jobEnable;
}
