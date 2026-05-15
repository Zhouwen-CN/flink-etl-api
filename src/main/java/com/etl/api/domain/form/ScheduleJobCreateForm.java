package com.etl.api.domain.form;

import com.etl.api.domain.validator.CronExpression;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增定时任务表单")
public class ScheduleJobCreateForm {

    @NotBlank
    @Length(max = 30)
    @Schema(description = "任务名称")
    private String name;

    @CronExpression
    @Schema(description = "cron 表达式")
    private String cronExpression;

    @NotNull
    @Schema(description = "ETL任务ID")
    private Long etlJobId;
}