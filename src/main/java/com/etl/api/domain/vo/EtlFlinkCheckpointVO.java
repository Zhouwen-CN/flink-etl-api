package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Flink检查点视图")
public class EtlFlinkCheckpointVO {

    @Schema(description = "检查点 id")
    private Integer chkId;

    @Schema(description = "类型(0检查点 1保存点)")
    private Boolean type;

    @Schema(description = "检查点路径")
    private String path;

    @Schema(description = "检查点触发时间")
    private LocalDateTime triggerTime;
}
