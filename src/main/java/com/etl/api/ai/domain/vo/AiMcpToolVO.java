package com.etl.api.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI MCP工具视图")
public class AiMcpToolVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "mcp服务id")
    private Long serverId;

    @Schema(description = "工具名")
    private String name;

    @Schema(description = "工具描述")
    private String description;

    @Schema(description = "自定义输出模板")
    private String outputTemplate;

    @Schema(description = "更新时间（兼作最近同步时间）")
    private LocalDateTime updateTime;
}
