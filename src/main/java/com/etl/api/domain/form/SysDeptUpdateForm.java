package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "更新部门表单")
public class SysDeptUpdateForm {

    @NotNull
    @Schema(description = "自增主键")
    private Long id;

    @NotBlank
    @Length(max = 100)
    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "显示排序")
    private Integer sort;

    // 不更新 parentId
}
