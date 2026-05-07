package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "权限视图")
public class PermissionVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "权限标识符")
    private String code;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
