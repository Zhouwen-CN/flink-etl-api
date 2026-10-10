package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "部门视图")
public class DeptVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "父部门id，根部门为 null")
    private Long parentId;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "显示排序")
    private Integer sort;

    @Schema(description = "创建者")
    private String createUser;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "子部门")
    private List<DeptVO> children;
}