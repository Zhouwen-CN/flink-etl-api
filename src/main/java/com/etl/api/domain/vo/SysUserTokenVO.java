package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "令牌视图")
public class SysUserTokenVO {

    @Schema(description = "访问令牌")
    private String token;
}
