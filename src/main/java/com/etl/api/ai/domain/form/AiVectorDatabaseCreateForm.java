package com.etl.api.ai.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "新增AI 向量数据库表单")
public class AiVectorDatabaseCreateForm {

    @NotBlank
    @Length(max = 50)
    @Schema(description = "向量数据库名称")
    private String name;

    @NotNull
    @Schema(description = "向量数据库类型（1-redis 2-milvus）")
    private Integer type;

    @Length(max = 255)
    @Schema(description = "向量数据库描述")
    private String description;

    @NotBlank
    @Schema(description = "连接配置JSON（按type结构不同：redis为 host、port、username、password；milvus为 host、port、username、password、databaseName）")
    private String configJson;
}
