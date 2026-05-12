package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典选择器视图")
public class DictionaryVO {

    @Schema(description = "标签")
    private String label;

    @Schema(description = "值")
    private Long value;
}
