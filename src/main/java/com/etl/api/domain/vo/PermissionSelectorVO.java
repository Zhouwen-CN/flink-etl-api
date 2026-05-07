package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "权限选择器视图")
public class PermissionSelectorVO {
    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "路由名称")
    private String routeName;

    @Schema(description = "操作类型")
    private String operationType;

}
