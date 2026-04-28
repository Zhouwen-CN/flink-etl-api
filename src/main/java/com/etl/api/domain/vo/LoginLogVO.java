package com.etl.api.domain.vo;

import com.etl.api.enumeration.LoginOperationEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "登入日志视图")
public class LoginLogVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "用户名称")
    private String username;

    @Schema(description = "操作类型")
    private LoginOperationEnum operation;

    @Schema(description = "ip地址")
    private String ip;

    @Schema(description = "地区")
    private String region;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
