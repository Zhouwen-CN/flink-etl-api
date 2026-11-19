package com.etl.api.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.ai.domain.form.AiVectorDatabaseCreateForm;
import com.etl.api.ai.domain.form.AiVectorDatabaseUpdateForm;
import com.etl.api.ai.domain.form.AiVectorDatabaseVerifyForm;
import com.etl.api.ai.domain.vo.AiVectorDatabaseVO;
import com.etl.api.ai.service.AiVectorDatabaseService;
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
@RequestMapping("/ai/vectordb")
@Tag(name = "AI 向量数据库控制器")
@RequiredArgsConstructor
public class AiVectorDatabaseController {

    private final AiVectorDatabaseService vectorDatabaseService;

    @SaCheckPermission("vectordb.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<AiVectorDatabaseVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "向量数据库名称") String searchName
    ) {
        return ResponseVO.ok(vectorDatabaseService.getPage(currentPage, pageSize, searchName));
    }

    @SaCheckPermission("vectordb.insert")
    @Operation(summary = "新增", description = "连接验证失败则整体保存失败，保证落库即连通")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated AiVectorDatabaseCreateForm form) {
        return vectorDatabaseService.addVectorDatabase(form);
    }

    @SaCheckPermission("vectordb.update")
    @Operation(summary = "更新", description = "type 或 config_json 变更时连接验证，失败整体更新失败保持旧值并失效旧缓存实例")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated AiVectorDatabaseUpdateForm form) {
        return vectorDatabaseService.updateVectorDatabase(form);
    }

    @SaCheckPermission("vectordb.update")
    @Operation(summary = "连接验证", description = "支持未保存配置，只读探测无写副作用")
    @PostMapping("/verify")
    public ResponseVO<Void> verify(@RequestBody @Validated AiVectorDatabaseVerifyForm form) {
        vectorDatabaseService.verifyConnection(form);
        return ResponseVO.ok();
    }

    @SaCheckPermission("vectordb.delete")
    @Operation(summary = "删除", description = "失效缓存实例并物理删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return vectorDatabaseService.removeVectorDatabase(id);
    }
}
