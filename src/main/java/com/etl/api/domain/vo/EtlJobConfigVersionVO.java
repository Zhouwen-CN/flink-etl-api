package com.etl.api.domain.vo;

import com.etl.api.enumeration.ConfigOperationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "ETL任务配置版本视图")
public class EtlJobConfigVersionVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "任务id")
    private Long jobId;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "json配置快照")
    private String config;

    @Schema(description = "操作类型(1创建 2更新 3回滚)")
    private ConfigOperationTypeEnum operationType;

    @Schema(description = "回滚来源版本号")
    private Integer sourceVersion;

    @Schema(description = "创建者")
    private String createUser;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
