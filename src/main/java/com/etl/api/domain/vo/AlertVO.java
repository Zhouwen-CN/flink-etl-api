package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "任务状态告警视图")
public class AlertVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "告警名称")
    private String name;

    @Schema(description = "邮件地址")
    private String email;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
