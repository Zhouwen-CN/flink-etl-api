package com.etl.api.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "AI MCP选择器视图（树形：server节点 -> tool叶子，供前端 el-tree-select 渲染）")
public class AiMcpSelectorVO {

    @Schema(description = "节点值：工具节点为工具id（可挂载bot），服务节点为 server-{id}（点击即全选其下所有工具）")
    private Object value;

    @Schema(description = "节点标签：服务节点为服务名，工具节点为工具名")
    private String label;

    @Schema(description = "子节点（工具列表），无工具时为空数组")
    private List<AiMcpSelectorVO> children;
}
