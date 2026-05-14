package com.etl.api.domain.vo;

import com.etl.api.enumeration.FlinkJobStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "ETL任务实例视图")
public class ETLJobInstanceVO {

    @Schema(description = "flink任务id")
    private String id;

    @Schema(description = "集群id")
    private Long clusterId;

    @Schema(description = "jar包id")
    private Long jarId;

    @Schema(description = "任务id")
    private Long jobId;

    @Schema(description = "任务类型(1batch 2streaming)")
    private Integer jobType;

    @Schema(description = "任务状态")
    private FlinkJobStatusEnum status;

    @Schema(description = "任务开始时间")
    private LocalDateTime startTime;

    @Schema(description = "任务结束时间")
    private LocalDateTime endTime;

    @Schema(description = "持续时间")
    private Long duration;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
