package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "编辑AI 知识库文档分块表单")
public class AiKnowledgeDocChunkUpdateForm {

    @NotNull
    @Schema(description = "分块ID")
    private Long id;

    @NotBlank
    @Schema(description = "分块内容（保存后同步重新向量化）")
    private String content;
}
