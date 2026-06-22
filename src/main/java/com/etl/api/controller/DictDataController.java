package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.convert.DictDataConvert;
import com.etl.api.domain.entity.DictData;
import com.etl.api.domain.form.DictDataCreateForm;
import com.etl.api.domain.form.DictDataUpdateForm;
import com.etl.api.domain.vo.DictDataVO;
import com.etl.api.domain.vo.DictionaryVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.service.DictDataService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
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
import java.util.List;

import static com.etl.api.domain.entity.table.DictDataTableDef.DICT_DATA;
import static com.etl.api.domain.entity.table.DictTypeTableDef.DICT_TYPE;

@RestController
@RequestMapping("/dict/data")
@Tag(name = "字典数据 控制器")
@RequiredArgsConstructor
public class DictDataController {
    private final DictDataService dictDataService;

    @SaCheckPermission("dict.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<DictDataVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam("typeId") @Parameter(description = "字典类型ID") Long typeId,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "字典名称") String searchName
    ) {

        val page = dictDataService.queryChain()
                .eq(DictData::getTypeId, typeId)
                .like(DictData::getLabel, searchName, StringUtils.hasText(searchName))
                .orderBy(DictData::getSortId, true)
                .pageAs(Page.of(currentPage, pageSize), DictDataVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("dict.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated DictDataCreateForm form) {
        return dictDataService.addDictData(form);

    }

    @SaCheckPermission("dict.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated DictDataUpdateForm form) {
        val entity = DictDataConvert.INSTANCE.convert(form);
        dictDataService.updateById(entity);
        return ResponseVO.ok();
    }

    @SaCheckPermission("dict.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        dictDataService.removeById(id);
        return ResponseVO.ok();
    }

    @SaCheckPermission("dict.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        dictDataService.removeByIds(ids);
        return ResponseVO.ok();
    }

    @Operation(summary = "获取字典")
    @GetMapping("/selector")
    public ResponseVO<List<DictionaryVO>> getDictDataSelector(@RequestParam("name") @Parameter(description = "字典名称") String name) {
        val queryWrapper = QueryWrapper.create()
                .select(DICT_DATA.LABEL, DICT_DATA.VALUE)
                .from(DICT_TYPE)
                .join(DICT_DATA)
                .on(DICT_TYPE.ID.eq(DICT_DATA.TYPE_ID))
                .where(DICT_TYPE.NAME.eq(name))
                .orderBy(DICT_DATA.SORT_ID, true);

        val vos = dictDataService.listAs(queryWrapper, DictionaryVO.class);
        return ResponseVO.ok(vos);
    }
}
