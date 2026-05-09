package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错误日志表 实体类。
 *
 * @author chen
 * @since 2026-05-09
 */
@Data
@Schema(description = "异常日志视图")
public class ErrorLogVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "请求地址")
    private String url;

    @Schema(description = "请求方式")
    private String method;

    @Schema(description = "ip地址")
    private String ip;

    @Schema(description = "地区")
    private String region;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "创建者")
    private String createUser;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
