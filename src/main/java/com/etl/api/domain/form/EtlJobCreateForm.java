package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Data
@Schema(description = "新增ETL任务表单")
public class EtlJobCreateForm {

    @NotBlank
    @Length(max = 100)
    @Schema(description = "任务名称")
    private String name;

    @NotNull
    @Schema(description = "任务类型")
    private Integer type;

    @NotNull
    @Schema(description = "flink集群id")
    private Long clusterId;

    @NotNull
    @Schema(description = "jar包id")
    private Long jarId;

    @NotNull
    @Min(value = 1)
    @Schema(description = "任务并行度")
    private Integer parallelism;

    // 最小10秒，最大5分钟
    @Range(min = 10000, max = 300000)
    @Schema(description = "检查点间隔")
    private Integer checkpointInterval;

    @NotBlank
    @Length(max = 65535)
    @Schema(description = "json配置")
    private String config;
}
