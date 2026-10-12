package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增ETL项目 表单")
public class EtlProjectCreateForm {

    @NotBlank
    @Length(max = 50)
    @Schema(description = "项目名称")
    private String name;

    @Length(max = 100)
    @Schema(description = "项目描述")
    private String description;

    @NotNull
    @Schema(description = "所属部门id（数据权限）")
    private Long deptId;
}
