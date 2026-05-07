package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色选择器视图")
public class RoleSelectorVO {

    @Schema(description = "角色名称")
    private String label;

    @Schema(description = "自增主键")
    private Long value;
}
