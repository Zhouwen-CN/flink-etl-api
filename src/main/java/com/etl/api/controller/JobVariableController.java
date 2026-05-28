package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.convert.JobVariableConvert;
import com.etl.api.domain.entity.JobVariable;
import com.etl.api.domain.form.JobVariableCreateForm;
import com.etl.api.domain.form.JobVariableUpdateForm;
import com.etl.api.domain.vo.JobVariableVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.service.JobVariableService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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

import java.util.Collection;

@RestController
@RequestMapping("/variable")
@Tag(name = "任务变量 控制器")
@RequiredArgsConstructor
public class JobVariableController {
    private final JobVariableService jobVariableService;

    @SaCheckPermission("variable.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<JobVariableVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "变量名称") String searchName
    ) {
        val page = jobVariableService.queryChain()
                .like(JobVariable::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), JobVariable.class);

        val list = page.getRecords()
                .stream()
                .map(JobVariableConvert.INSTANCE::convert)
                .toList();

        return ResponseVO.ok(new PageVO<>(list, page.getTotalPage()));
    }

    @SaCheckPermission("variable.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated JobVariableCreateForm form) {
        return jobVariableService.addJobVar(form);
    }

    @SaCheckPermission("variable.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated JobVariableUpdateForm form) {
        val entity = JobVariableConvert.INSTANCE.convert(form);
        jobVariableService.updateById(entity);
        return ResponseVO.ok();
    }

    @SaCheckPermission("variable.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        jobVariableService.removeById(id);
        return ResponseVO.ok();
    }

    @SaCheckPermission("variable.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        jobVariableService.removeByIds(ids);
        return ResponseVO.ok();
    }
}
