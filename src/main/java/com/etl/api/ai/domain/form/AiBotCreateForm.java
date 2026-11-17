package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增AI Bot表单")
public class AiBotCreateForm {

    @NotNull
    @Schema(description = "模型ID")
    private Long modelId;

    @NotBlank
    @Length(max = 50)
    @Schema(description = "bot名称")
    private String name;

    @DecimalMin(value = "0")
    @DecimalMax(value = "2")
    @Schema(description = "模型温度(0-2之间)")
    private Double temperature;

    @DecimalMin(value = "0")
    @DecimalMax(value = "1")
    @Schema(description = "Top-P(0-1之间)")
    private Double topP;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @NotNull
    @PositiveOrZero
    @Schema(description = "单次对话最大工具调用轮数")
    private Integer maxToolCalls;

    @Schema(description = "状态: 1-启用 0-停用")
    private Boolean status;

    @NotNull
    @Schema(description = "归属部门id")
    private Long deptId;
}
