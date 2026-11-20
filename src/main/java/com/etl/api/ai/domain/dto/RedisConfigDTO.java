package com.etl.api.ai.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Redis 向量数据库连接配置（config_json 平铺格式的强类型映射）。
 *
 * <p>连接目标必须是 Redis Stack（RediSearch + RedisJSON），
 * 普通 Redis 在连接验证阶段会因缺少 ft 命令而失败。
 *
 * @author chen
 * @since 2026-11-18
 */
@Data
@Schema(description = "Redis 向量数据库连接配置")
public class RedisConfigDTO implements AiVectorDbConfig {

    @Schema(description = "Redis 主机")
    private String host;

    @Schema(description = "Redis 端口（host 模式下必填）")
    private Integer port;

    @Schema(description = "用户名，可选")
    private String username;

    @Schema(description = "密码，可选")
    private String password;

    @Override
    public void validate() {
        validateCommon();
    }
}
