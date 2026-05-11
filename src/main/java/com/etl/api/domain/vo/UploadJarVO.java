package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 上传jar包表 实体类。
 *
 * @author chen
 * @since 2026-05-11
 */
@Data
@Schema(description = "jar包视图")
public class UploadJarVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "jar包名称")
    private String name;

    @Schema(description = "jar包地址")
    private String path;

    @Schema(description = "入口类")
    private String mainClass;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
