package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增部门表单")
public class SysDeptCreateForm {

    @NotBlank
    @Length(max = 100)
    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "显示排序")
    private Integer sort;

    @Schema(description = "父部门id，根部门为 null")
    private Long parentId;
}
