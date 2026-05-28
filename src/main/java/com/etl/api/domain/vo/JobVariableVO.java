package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Schema(description = "任务变量视图")
public class JobVariableVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "变量名")
    private String name;

    @Schema(description = "变量值(支持SPEL表达式)")
    private String value;

    @Schema(description = "真实值")
    private String realValue;

    @Schema(description = "是否启用")
    private Boolean status;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
