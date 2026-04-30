package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色视图")
public class RoleVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色标识符")
    private String code;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
