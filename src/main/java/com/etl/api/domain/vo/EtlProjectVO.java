package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "ETL项目分页 视图")
public class EtlProjectVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "项目名称")
    private String name;

    @Schema(description = "项目描述")
    private String description;

    @Schema(description = "创建者")
    private String createUser;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
