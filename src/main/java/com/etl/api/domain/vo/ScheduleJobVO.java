package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "定时任务视图")
public class ScheduleJobVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "cron 表达式")
    private String cronExpression;

    @Schema(description = "ETL任务ID")
    private Long etlJobId;

    @Schema(description = "是否开启")
    private Boolean jobEnable;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}