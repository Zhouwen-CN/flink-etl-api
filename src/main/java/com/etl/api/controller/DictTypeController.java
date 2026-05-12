package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.convert.DictTypeConvert;
import com.etl.api.domain.entity.DictType;
import com.etl.api.domain.form.DictTypeCreateForm;
import com.etl.api.domain.form.DictTypeUpdateForm;
import com.etl.api.domain.vo.DictTypeVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.service.DictTypeService;
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
@RequestMapping("/dict/type")
@Tag(name = "字典类型 控制器")
@RequiredArgsConstructor
public class DictTypeController {
    private final DictTypeService dictTypeService;

    @SaCheckPermission("dict.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<DictTypeVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "字典名称") String searchName
    ) {
        val page = dictTypeService.queryChain()
                .like(DictType::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), DictTypeVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("dict.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated DictTypeCreateForm form) {
        return dictTypeService.addDictType(form);
    }

    @SaCheckPermission("dict.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated DictTypeUpdateForm form) {
        val entity = DictTypeConvert.INSTANCE.convert(form);
        dictTypeService.updateById(entity);
        return ResponseVO.ok();
    }

    @SaCheckPermission("dict.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return dictTypeService.removeDictType(id);
    }

    @SaCheckPermission("dict.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        return dictTypeService.removeBatchDictType(ids);
    }
}