package com.etl.api.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.ai.domain.form.AiMcpToolUpdateForm;
import com.etl.api.ai.domain.vo.AiMcpToolVO;
import com.etl.api.ai.service.AiMcpToolService;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ai/mcp/tool")
@Tag(name = "AI MCP 工具控制器")
@RequiredArgsConstructor
public class AiMcpToolController {

    private final AiMcpToolService mcpToolService;

    @SaCheckPermission("mcp.select")
    @Operation(summary = "分页查询", description = "分页查询指定 MCP 服务的工具清单")
    @GetMapping
    public ResponseVO<PageVO<AiMcpToolVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam("serverId") @Parameter(description = "mcp服务id") @NotNull Long serverId,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "工具名") String searchName
    ) {
        return ResponseVO.ok(mcpToolService.getPage(currentPage, pageSize, serverId, searchName));
    }

    @SaCheckPermission("mcp.update")
    @Operation(summary = "更新", description = "更新工具用户可配置字段（outputTemplate，传null表示清空）")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated AiMcpToolUpdateForm form) {
        return mcpToolService.updateAiMcpTool(form);
    }
}
