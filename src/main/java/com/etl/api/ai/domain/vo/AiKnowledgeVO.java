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

    @Schema(description = "单个分块的最大字符数")
    private Integer chunkSize;

    @Schema(description = "相邻分块的重叠字符数")
    private Integer overlapSize;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
