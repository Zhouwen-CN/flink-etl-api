package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增AI 知识库表单")
public class AiKnowledgeCreateForm {

    @NotBlank
    @Length(max = 50)
    @Schema(description = "知识库名称")
    private String name;

    @Length(max = 255)
    @Schema(description = "知识库描述")
    private String description;

    @Min(50)
    @Max(10000)
    @Schema(description = "单个分块的最大字符数（默认800）")
    private Integer chunkSize;

    @Min(0)
    @Max(2000)
    @Schema(description = "相邻分块的重叠字符数（默认200，须小于chunkSize）")
    private Integer overlapSize;
}
