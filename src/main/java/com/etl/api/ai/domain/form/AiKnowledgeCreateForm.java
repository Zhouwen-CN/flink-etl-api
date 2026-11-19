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

    @NotNull
    @Schema(description = "向量数据库id")
    private Long vectorDbId;

    @NotNull
    @Schema(description = "Embedding模型id")
    private Long embedModelId;

    @Min(1)
    @Max(100)
    @Schema(description = "最大召回条数（默认5）")
    private Integer recallTopK;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    @Schema(description = "相似度下限，0~1（默认0.20）")
    private Double minScore;
}
