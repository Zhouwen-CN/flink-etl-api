package com.etl.api.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI 知识库文档分块视图")
public class AiKnowledgeDocChunkVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "向量库文档ID")
    private String vectorId;

    @Schema(description = "文档id")
    private Long documentId;

    @Schema(description = "知识库id")
    private Long knowledgeId;

    @Schema(description = "分块内容")
    private String content;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
