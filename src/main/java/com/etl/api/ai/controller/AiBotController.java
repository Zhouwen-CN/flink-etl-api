package com.etl.api.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.ai.domain.convert.AiModelConvert;
import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.form.AiBotCreateForm;
import com.etl.api.ai.domain.form.AiBotUpdateForm;
import com.etl.api.ai.domain.vo.AiBotVO;
import com.etl.api.ai.service.AiBotService;
import com.etl.api.ai.service.AiModelService;
import com.etl.api.sys.domain.vo.DictionaryVO;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.etl.api.sys.domain.vo.SysDeptVO;
import com.etl.api.sys.service.SysDeptService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.StringUtils;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ai/bot")
@Tag(name = "AI Bot 控制器")
@RequiredArgsConstructor
public class AiBotController {

    private final AiBotService botService;
    private final AiModelService modelService;
    private final SysDeptService deptService;

    @SaCheckPermission("bot.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<AiBotVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "bot名称") String searchName,
            @RequestParam(value = "modelId", required = false) @Parameter(description = "模型ID") Long modelId,
            @RequestParam(value = "status", required = false) @Parameter(description = "状态: 1-启用 0-停用") Boolean status
    ) {
        val page = botService.queryChain()
                .like(AiBot::getName, searchName, StringUtils.hasText(searchName))
                .eq(AiBot::getModelId, modelId, modelId != null)
                .eq(AiBot::getStatus, status, status != null)
                .pageAs(Page.of(currentPage, pageSize), AiBotVO.class);
        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("bot.select")
    @Operation(summary = "模型选择器")
    @GetMapping("/model/selector")
    public ResponseVO<List<DictionaryVO>> getModelSelector() {
        val vos = modelService.list().stream()
                .map(AiModelConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("bot.select")
    @Operation(summary = "部门选择器")
    @GetMapping("/dept/selector")
    public ResponseVO<List<SysDeptVO>> deptSelector() {
        val vos = deptService.treeSelector();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("bot.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated AiBotCreateForm form) {
        return botService.addAiBot(form);
    }

    @SaCheckPermission("bot.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated AiBotUpdateForm form) {
        return botService.updateAiBot(form);
    }

    @SaCheckPermission("bot.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        botService.removeById(id);
        return ResponseVO.ok();
    }
}
