package com.etl.api.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI MCP服务视图")
public class AiMcpServerVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "mcp服务名称")
    private String name;

    @Schema(description = "mcp服务描述")
    private String description;

    @Schema(description = "mcp连接配置JSON")
    private String configJson;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
