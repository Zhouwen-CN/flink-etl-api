package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Data
@Schema(description = "新增角色表单")
public class SysRoleCreateForm {

    @NotNull
    @Length(max = 30)
    @Schema(description = "角色名称")
    private String name;

    @NotNull
    @Length(max = 30)
    @Schema(description = "角色标识符")
    private String code;

    @NotNull
    @Range(min = 1, max = 3)
    @Schema(description = "数据范围(1全部 2本部门及以下 3本部门)")
    private Integer dataScope;

    @NotNull
    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;
}
