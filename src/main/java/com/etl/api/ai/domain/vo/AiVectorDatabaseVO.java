package com.etl.api.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI 向量数据库视图")
public class AiVectorDatabaseVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "向量数据库名称")
    private String name;

    @Schema(description = "向量数据库类型（1-redis 2-milvus）")
    private Integer type;

    @Schema(description = "向量数据库描述")
    private String description;

    @Schema(description = "连接配置JSON")
    private String configJson;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
