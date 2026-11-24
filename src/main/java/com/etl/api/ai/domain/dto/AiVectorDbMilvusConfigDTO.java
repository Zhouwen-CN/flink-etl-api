package com.etl.api.ai.domain.dto;

import com.etl.api.ai.exception.AiVectorDbConfigException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * Milvus 向量数据库连接配置（config_json 平铺格式的强类型映射）。
 *
 * <p>username/password 可选，但必须成对提供（Milvus 开启鉴权时必填）。
 *
 * @author chen
 * @since 2026-11-19
 */
@Data
@Schema(description = "Milvus 向量数据库连接配置")
public class AiVectorDbMilvusConfigDTO implements AiVectorDbConfig {

    @Schema(description = "Milvus 主机（必填）")
    private String host;

    @Schema(description = "Milvus 端口（必填，gRPC 端口默认 19530）")
    private Integer port;

    @Schema(description = "用户名，可选（与 password 成对提供）")
    private String username;

    @Schema(description = "密码，可选（与 username 成对提供）")
    private String password;

    @Schema(description = "数据库名，可选（默认 default）")
    private String databaseName;

    @Override
    public void validate() {
        validateCommon();
        // 鉴权信息必须成对出现（Milvus 开启鉴权时 username/password 同时必填）
        boolean hasUsername = StringUtils.hasText(username);
        boolean hasPassword = StringUtils.hasText(password);
        if (hasUsername != hasPassword) {
            throw new AiVectorDbConfigException("Milvus 连接配置 username 与 password 必须成对提供");
        }
    }
}
