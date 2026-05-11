package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Flink 集群视图")
public class FlinkClusterVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "集群名称")
    private String name;

    @Schema(description = "集群ip地址")
    private String ip;

    @Schema(description = "集群端口")
    private Integer port;

    @Schema(description = "Flink 版本")
    private String version;

    @Schema(description = "集群状态")
    private Boolean status;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
