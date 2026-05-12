package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "ETL任务视图")
public class ETLJobVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "flink集群id")
    private Long clusterId;

    @Schema(description = "jar包id")
    private Long jarId;

    @Schema(description = "json配置")
    private String config;

    @Schema(description = "任务类型")
    private Integer type;

    @Schema(description = "任务状态")
    private String status;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
