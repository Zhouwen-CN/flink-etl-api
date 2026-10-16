package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.form.SysDeptCreateForm;
import com.etl.api.domain.form.SysDeptUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.SysDeptVO;
import com.etl.api.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
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

@RestController
@RequestMapping("/dept")
@Tag(name = "部门 控制器")
@RequiredArgsConstructor
public class DeptController {

    private final SysDeptService deptService;

    @SaCheckPermission("dept.select")
    @Operation(summary = "部门树")
    @GetMapping("/tree")
    public ResponseVO<List<SysDeptVO>> tree(@RequestParam(value = "name", required = false) @Parameter(description = "部门名称") String name) {
        val vos = deptService.listTree(name);
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("dept.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated SysDeptCreateForm form) {
        return deptService.addDept(form);
    }

    @SaCheckPermission("dept.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated SysDeptUpdateForm form) {
        return deptService.modifyDept(form);
    }

    @SaCheckPermission("dept.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return deptService.removeDept(id);
    }
}