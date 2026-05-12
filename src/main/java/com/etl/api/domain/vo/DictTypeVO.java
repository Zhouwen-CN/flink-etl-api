package com.etl.api.domain.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "字典类型视图")
public class DictTypeVO {

    @Id(keyType = KeyType.Auto)
    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "字典名称")
    private String name;

    @Schema(description = "字典描述")
    private String remark;

    @Schema(description = "字典是否启用")
    private Boolean status;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
