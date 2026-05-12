package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典数据视图")
public class DictDataVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "字典类型id")
    private Long typeId;

    @Schema(description = "字典键")
    private String label;

    @Schema(description = "字典值")
    private Long value;

    @Schema(description = "字典排序")
    private Integer sortId;
}
