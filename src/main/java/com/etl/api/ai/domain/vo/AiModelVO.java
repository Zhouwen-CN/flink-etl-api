package com.etl.api.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI模型视图")
public class AiModelVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "模型唯一标识")
    private String name;

    @Schema(description = "接口格式: 1-OpenAI Chat Completions, 2-OpenAI Responses, 3-Anthropic Messages")
    private Integer format;

    @Schema(description = "大模型请求地址")
    private String baseUrl;

    @Schema(description = "模型名称，如 gpt-4o-mini")
    private String modelName;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
