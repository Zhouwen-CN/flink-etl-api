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
public class SysRoleVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色标识符")
    private String code;

    @Schema(description = "数据范围(1全部 2本部门及以下 3本部门)")
    private Integer dataScope;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
