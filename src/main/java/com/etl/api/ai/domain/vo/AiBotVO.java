package com.etl.api.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI Bot视图")
public class AiBotVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "bot名称")
    private String name;

    @Schema(description = "模型温度(0-2之间)")
    private Double temperature;

    @Schema(description = "Top-P(0-1之间)")
    private Double topP;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "单次对话最大工具调用轮数")
    private Integer maxToolCalls;

    @Schema(description = "状态: 1-启用 0-停用")
    private Boolean status;

    @Schema(description = "归属部门id")
    private Long deptId;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
