package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.convert.EtlProjectConvert;
import com.etl.api.domain.entity.EtlProject;
import com.etl.api.domain.form.EtlProjectCreateForm;
import com.etl.api.domain.form.EtlProjectUpdateForm;
import com.etl.api.domain.vo.EtlProjectVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.service.EtlProjectService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/project")
@Tag(name = "ETL项目 控制器")
@RequiredArgsConstructor
public class EtlProjectController {
    private final EtlProjectService etlProjectService;

    @SaCheckPermission("project.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<EtlProjectVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "name", required = false) @Parameter(description = "项目名称") String name
    ) {
        val page = etlProjectService.queryChain()
                .like(EtlProject::getName, name, StringUtils.hasText(name))
                .pageAs(Page.of(currentPage, pageSize), EtlProjectVO.class);
        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("project.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated EtlProjectCreateForm form) {
        return etlProjectService.addProject(form);
    }

    @SaCheckPermission("project.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated EtlProjectUpdateForm form) {
        val entity = EtlProjectConvert.INSTANCE.convert(form);
        etlProjectService.updateById(entity);
        return ResponseVO.ok();
    }

    @SaCheckPermission("project.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return etlProjectService.removeProject(id);
    }
}
