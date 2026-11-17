package com.etl.api.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.ai.domain.form.AiMcpServerCreateForm;
import com.etl.api.ai.domain.form.AiMcpServerUpdateForm;
import com.etl.api.ai.domain.vo.AiMcpServerVO;
import com.etl.api.ai.service.AiMcpServerService;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ai/mcp")
@Tag(name = "AI MCP 服务控制器")
@RequiredArgsConstructor
public class AiMcpServerController {

    private final AiMcpServerService mcpServerService;

    @SaCheckPermission("mcp.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<AiMcpServerVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "mcp服务名称") String searchName
    ) {
        return ResponseVO.ok(mcpServerService.getPage(currentPage, pageSize, searchName));
    }

    @SaCheckPermission("mcp.insert")
    @Operation(summary = "新增", description = "校验 config_json + 同步工具，同步失败整体失败")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated AiMcpServerCreateForm form) {
        return mcpServerService.addAiMcpServer(form);
    }

    @SaCheckPermission("mcp.update")
    @Operation(summary = "更新", description = "config_json 变更时同步工具，同步失败整体失败保持旧值")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated AiMcpServerUpdateForm form) {
        return mcpServerService.updateAiMcpServer(form);
    }

    @SaCheckPermission("mcp.update")
    @Operation(summary = "手动同步工具", description = "临时连接拉取工具清单并 upsert，失败仅返回错误原因不回滚数据")
    @PostMapping("/sync/{id}")
    public ResponseVO<Void> sync(@PathVariable @Parameter(description = "ID") Long id) {
        return mcpServerService.syncTools(id);
    }

    @SaCheckPermission("mcp.delete")
    @Operation(summary = "删除", description = "同时物理删除该服务的工具缓存并级联清理挂载行")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return mcpServerService.removeAiMcpServer(id);
    }
}
