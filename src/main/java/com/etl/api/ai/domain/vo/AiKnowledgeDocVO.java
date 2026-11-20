package com.etl.api.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI 知识库文档视图")
public class AiKnowledgeDocVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "知识库id")
    private Long knowledgeId;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "解析状态：WAITING/PROCESSING/SUCCESS/FAILED")
    private String parseStatus;

    @Schema(description = "解析失败原因")
    private String parseFailReason;

    @Schema(description = "分块数量")
    private Long chunkCount;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
