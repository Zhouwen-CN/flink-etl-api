package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字典选择器视图")
public class DictionaryVO {

    @Schema(description = "标签")
    private String label;

    @Schema(description = "值")
    private Object value;
}
