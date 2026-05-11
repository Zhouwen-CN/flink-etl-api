package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "修改Flink集群表单")
public class FlinkClusterUpdateForm extends FlinkClusterCreateForm {

    @NotNull
    @Schema(description = "集群ID")
    private Long id;
}
