package com.etl.api.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI 知识库视图")
public class AiKnowledgeVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "知识库名称")
    private String name;

    @Schema(description = "知识库描述")
    private String description;

    @Schema(description = "向量数据库id")
    private Long vectorDbId;

    @Schema(description = "Embedding模型id")
    private Long embedModelId;

    @Schema(description = "向量维度")
    private Integer dimension;

    @Schema(description = "最大召回条数")
    private Integer recallTopK;

    @Schema(description = "相似度下限，0~1")
    private Double minScore;

    @Schema(description = "单个分块的最大字符数")
    private Integer chunkSize;

    @Schema(description = "相邻分块的重叠字符数")
    private Integer overlapSize;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
