package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "请求日志视图")
public class RequestLogVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "请求url")
    private String url;

    @Schema(description = "请求ip")
    private String ip;

    @Schema(description = "地区")
    private String region;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "响应状态码")
    private Integer status;

    @Schema(description = "花费时间")
    private Long takenTime;

    @Schema(description = "请求用户")
    private String createUser;

    @Schema(description = "请求时间")
    private LocalDateTime createTime;
}
